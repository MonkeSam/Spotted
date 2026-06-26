package com.example.spotted.data.repository

import com.example.spotted.data.model.Category
import com.example.spotted.data.remote.SupabaseModule
import com.example.spotted.utils.Resource
import io.github.jan.supabase.postgrest.from

class CategoryRepository {
    private val db get() = SupabaseModule.client.from("Categories")

    suspend fun getAllCategories(): Resource<List<Category>> = try {
        Resource.Success(db.select().decodeList<Category>())
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero delle categorie")
    }

    suspend fun getCategoryMap(): Resource<Map<Int, Category>> = try {
        Resource.Success(db.select().decodeList<Category>().associateBy { it.id })
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Errore nel recupero della mappa categorie")
    }
}