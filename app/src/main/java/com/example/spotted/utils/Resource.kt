package com.example.spotted.utils

sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

fun <T> Result<T>.toResource(): Resource<T> = fold(
    onSuccess = { Resource.Success(it) },
    onFailure = { Resource.Error(it.message?: "Errore sconosciuto") }
)