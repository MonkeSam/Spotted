package com.example.spotted.data.repository

import com.example.spotted.data.model.User
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.result.PostgrestResult

object UserRepository {

    private val auth get() = SupabaseModule.client.auth
    private val db   get() = SupabaseModule.client.from("Users")

    // Funzione privata che può lanciare, usata internamente
    private suspend fun fetchCurrentUser(): User {
        val uid = auth.currentUserOrNull()?.id
            ?: throw Exception("Nessun utente autenticato")
        return db.select { filter { eq("id", uid) } }.decodeSingle<User>()
    }

    suspend fun login(email: String, password: String): Resource<User> = runCatching {
        auth.signInWith(Email) {
            this.email    = email
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
        name: String?           = null,
        surname: String?        = null,
        profilePicture: String? = null
    ): Resource<PostgrestResult> = runCatching {
        db.update({
            name?.let           { set("name", it) }
            surname?.let        { set("surname", it) }
            profilePicture?.let { set("profile_picture", it) }
        }) {
            filter { eq("id", userId) }
        }
    }.toResource()
}