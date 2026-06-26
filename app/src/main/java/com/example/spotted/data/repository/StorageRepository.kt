package com.example.spotted.data.repository

import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

class StorageRepository {
    private val BUCKET = "Pictures"
    private val POSTS_FOLDER = "Posts"

    suspend fun uploadPostPhoto(
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Resource<String> = try {
        val extension = when {
            mimeType.contains("png", ignoreCase = true) -> "png"
            mimeType.contains("webp", ignoreCase = true) -> "webp"
            else -> "jpg"
        }
        val filename = "${UUID.randomUUID()}.$extension"
        val storagePath = "$POSTS_FOLDER/$filename"

        SupabaseModule.client.storage
            .from(BUCKET)
            .upload(storagePath, bytes) {
                upsert = false
                contentType = ContentType.parse(mimeType)
            }

        val url = SupabaseModule.client.storage
            .from(BUCKET)
            .publicUrl(storagePath)
        Resource.Success(url)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel caricamento dell'immagine")
    }

    suspend fun deletePostPhoto(storagePath: String): Resource<Unit> = try {
        SupabaseModule.client.storage
            .from(BUCKET)
            .delete(listOf(storagePath))
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nella cancellazione dell'immagine")
    }
}