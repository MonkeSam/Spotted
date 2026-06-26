// StorageRepository.kt
package com.example.spotted.data.repository

import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

/**
 * Repository per la gestione dei file nel bucket Supabase Storage "Pictures".
 *
 * Struttura bucket:
 *  - Bucket:  "Pictures"
 *  - Cartella post: "Posts/"
 *
 * Il bucket deve essere configurato come PUBLIC su Supabase per poter
 * generare URL pubblici con publicUrl().
 */
class StorageRepository {

    private val BUCKET = "Pictures"
    private val POSTS_FOLDER = "Posts"

    /**
     * Carica una foto nel percorso Pictures/Posts/<uuid>.<ext> e
     * restituisce l'URL pubblico del file caricato.
     *
     * @param bytes     byte array del file immagine
     * @param mimeType  tipo MIME dell'immagine (es. "image/jpeg", "image/png")
     * @return Resource.Success con l'URL pubblico, o Resource.Error con il messaggio
     */
    suspend fun uploadPostPhoto(
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Resource<String> = runCatching {
        val extension = when {
            mimeType.contains("png",  ignoreCase = true) -> "png"
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

        // Restituisce l'URL pubblico del file appena caricato
        SupabaseModule.client.storage
            .from(BUCKET)
            .publicUrl(storagePath)
    }.toResource()

    /**
     * Elimina una foto dal bucket dato il suo percorso relativo
     * (es. "Posts/abc123.jpg").
     */
    suspend fun deletePostPhoto(storagePath: String): Resource<Unit> = runCatching {
        SupabaseModule.client.storage
            .from(BUCKET)
            .delete(listOf(storagePath))
    }.toResource()
}
