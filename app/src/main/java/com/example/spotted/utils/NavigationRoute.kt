package com.example.spotted.utils

import kotlinx.serialization.Serializable

sealed interface NavigationRoute{
    @Serializable data object Feed : NavigationRoute
    @Serializable data object  Following : NavigationRoute
    @Serializable data object Map : NavigationRoute
    @Serializable data object Profile : NavigationRoute
}