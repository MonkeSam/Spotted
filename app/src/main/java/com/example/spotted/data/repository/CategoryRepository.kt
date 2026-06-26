package com.example.spotted.data.repository

import com.example.spotted.data.model.Category
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import com.example.spotted.utils.toResource
import io.github.jan.supabase.postgrest.from

class CategoryRepository {

    private val db get() = SupabaseModule.client.from("Categories")

    suspend fun getAllCategories(): Resource<List<Category>> = runCatching {
        db.select().decodeList<Category>()
    }.toResource()

    /** Ritorna una mappa id → Category per lookup O(1) nei ViewModel/composable. */
    suspend fun getCategoryMap(): Resource<Map<Int, Category>> = runCatching {
        db.select().decodeList<Category>().associateBy { it.id }
    }.toResource()
}
