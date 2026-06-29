package com.example.spotted.utils

import kotlinx.serialization.Serializable

sealed interface NavigationRoute{
    @Serializable data object Feed : NavigationRoute
    @Serializable data object  Following : NavigationRoute
    @Serializable data object Map : NavigationRoute
    @Serializable data object Profile : NavigationRoute

    @Serializable data object Login : NavigationRoute

    @Serializable data class Chat(val chatId: Long) : NavigationRoute

    @Serializable data object Share: NavigationRoute

    @Serializable data object Signup: NavigationRoute

    @Serializable data class NoGpsMap(val permanentlyDenied: Boolean = false) : NavigationRoute
}