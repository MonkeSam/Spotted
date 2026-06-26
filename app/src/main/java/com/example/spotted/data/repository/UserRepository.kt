package com.example.spotted.data.repository

import com.example.spotted.data.model.User
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult
import io.github.jan.supabase.storage.storage

class UserRepository {

    private val auth get() = SupabaseModule.client.auth
    private val db get() = SupabaseModule.client.from("Users")

    // Funzione privata che può lanciare, usata internamente
    private suspend fun fetchCurrentUser(): User {
        val uid = auth.currentUserOrNull()?.id
            ?: throw Exception("Nessun utente autenticato")
        return db.select { filter { eq("id", uid) } }.decodeSingle<User>()
    }

    suspend fun login(email: String, password: String): Resource<User> = runCatching {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        fetchCurrentUser() // lancia se fallisce, catturato da runCatching
    }.toResource()

    suspend fun getCurrentUser(): Resource<User> = runCatching {
        fetchCurrentUser()
    }.toResource()

    suspend fun logout(): Resource<Unit> = runCatching {
        auth.signOut()
    }.toResource()

    suspend fun getUserById(id: String): Resource<User> = runCatching {
        db.select { filter { eq("id", id) } }.decodeSingle<User>()
    }.toResource()

    suspend fun updateProfile(
        userId: String,
        name: String? = null,
        surname: String? = null,
        profilePicture: String? = null
    ): Resource<PostgrestResult> = runCatching {
        db.update({
            name?.let { set("name", it) }
            surname?.let { set("surname", it) }
            profilePicture?.let { set("profile_picture", it) }
        }) {
            filter { eq("id", userId) }
        }
    }.toResource()

    private val storage get() = SupabaseModule.client.storage

    suspend fun uploadProfilePicture(
        bytes: ByteArray,
        mimeType: String,
        userId: String
    ): Resource<String> = runCatching {
        val extension = if (mimeType.contains("png", ignoreCase = true)) "png" else "jpg"
        val path = "Profile/$userId.$extension"

        val bucket = storage.from("Pictures")

        // Carica e sovrascrive il file
        bucket.upload(path, bytes) { upsert = true }

        // Recupera l'URL pubblico base
        val baseUrl = bucket.publicUrl(path)

        // TRUCCO ANTI-CACHE: Aggiungiamo i millisecondi correnti all'URL
        "$baseUrl?t=${System.currentTimeMillis()}"
    }.toResource()

    // In UserRepository.kt, aggiorna la firma e il corpo della funzione così:

    suspend fun register(
        email: String,
        password: String,
        name: String,
        surname: String,
        profilePictureBytes: ByteArray? = null, // Aggiungi questo
        mimeType: String = "image/jpeg"         // Aggiungi questo
    ): Resource<User> = runCatching {

        // 1. Auth SignUp
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val uid = auth.currentUserOrNull()?.id ?: throw Exception("Errore Auth")

        // 2. LOGICA IMMAGINE: Carica se i byte sono presenti
        var profilePicUrl: String? = null
        if (profilePictureBytes != null) {
            // Usa la funzione che hai già nel repository
            val uploadResult = uploadProfilePicture(profilePictureBytes, mimeType, uid)
            if (uploadResult is Resource.Success) {
                profilePicUrl = uploadResult.data
            } else {
                throw Exception("Errore upload immagine: ${(uploadResult as Resource.Error).message}")
            }
        }

        // 3. Inserimento nel DB con l'URL della foto
        val newUser = User(
            id = uid,
            email = email,
            name = name,
            surname = surname,
            profilePicture = profilePicUrl // Assicurati che questo campo esista nella data class User
        )

        db.insert(newUser)
        newUser
    }.toResource()

    suspend fun getUsersByIds(ids: List<String>): Resource<List<User>> = runCatching {
        if (ids.isEmpty()) return@runCatching emptyList()
        db.select {
            filter { isIn("id", ids) } // supabase-postgrest supporta isIn
        }.decodeList<User>()
    }.toResource()
}