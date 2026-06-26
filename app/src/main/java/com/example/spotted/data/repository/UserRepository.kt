package com.example.spotted.data.repository

import com.example.spotted.data.model.User
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.storage.storage

class UserRepository {
    private val auth get() = SupabaseModule.client.auth
    private val db get() = SupabaseModule.client.from("Users")

    private suspend fun fetchCurrentUser(): User {
        val uid = auth.currentUserOrNull()?.id
            ?: throw Exception("Nessun utente autenticato")
        return db.select { filter { eq("id", uid) } }.decodeSingle<User>()
    }

    suspend fun login(email: String, password: String): Resource<User> = try {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        Resource.Success(fetchCurrentUser())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel login")
    }

    suspend fun getCurrentUser(): Resource<User> = try {
        Resource.Success(fetchCurrentUser())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dell'utente corrente")
    }

    suspend fun logout(): Resource<Unit> = try {
        auth.signOut()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel logout")
    }

    suspend fun getUserById(id: String): Resource<User> = try {
        Resource.Success(db.select { filter { eq("id", id) } }.decodeSingle<User>())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero dell'utente per ID")
    }

    suspend fun updateProfile(
        userId: String,
        name: String? = null,
        surname: String? = null,
        profilePicture: String? = null
    ): Resource<PostgrestResult> = try {
        Resource.Success(
            db.update({
                name?.let { set("name", it) }
                surname?.let { set("surname", it) }
                profilePicture?.let { set("profile_picture", it) }
            }) {
                filter { eq("id", userId) }
            }
        )
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nell'aggiornamento del profilo")
    }

    private val storage get() = SupabaseModule.client.storage

    suspend fun uploadProfilePicture(
        bytes: ByteArray,
        mimeType: String,
        userId: String
    ): Resource<String> = try {
        val extension = if (mimeType.contains("png", ignoreCase = true)) "png" else "jpg"
        val path = "Profile/$userId.$extension"
        val bucket = storage.from("Pictures")
        bucket.upload(path, bytes) { upsert = true }
        val baseUrl = bucket.publicUrl(path)
        Resource.Success("$baseUrl?t=${System.currentTimeMillis()}")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel caricamento dell'immagine profilo")
    }

    suspend fun register(
        email: String,
        password: String,
        name: String,
        surname: String,
        profilePictureBytes: ByteArray? = null,
        mimeType: String = "image/jpeg"
    ): Resource<User> = try {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        val uid = auth.currentUserOrNull()?.id ?: throw Exception("Errore Auth")
        var profilePicUrl: String? = null
        if (profilePictureBytes != null) {
            val uploadResult = uploadProfilePicture(profilePictureBytes, mimeType, uid)
            if (uploadResult is Resource.Success) {
                profilePicUrl = uploadResult.data
            } else {
                throw Exception("Errore upload immagine: ${(uploadResult as Resource.Error).message}")
            }
        }
        val newUser = User(
            id = uid,
            email = email,
            name = name,
            surname = surname,
            profilePicture = profilePicUrl
        )
        db.insert(newUser)
        Resource.Success(newUser)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nella registrazione")
    }

    suspend fun getUsersByIds(ids: List<String>): Resource<List<User>> = try {
        if (ids.isEmpty()) {
            Resource.Success(emptyList())
        } else {
            Resource.Success(
                db.select {
                    filter { isIn("id", ids) }
                }.decodeList<User>()
            )
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero degli utenti per ID")
    }
}