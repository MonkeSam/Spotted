package com.example.spotted.data.model

import com.example.spotted.utils.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class User(
    val id: String = "",
    val email: String,
    val name: String? = null,
    val surname: String? = null,
    val birthday: String? = null,
    @SerialName("profile_picture") val profilePicture: String? = null
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val emoji: String
)

@OptIn(ExperimentalTime::class)
@Serializable
data class Post(
    val id: Long = 0,
    val category: Int,
    val title: String? = null,
    val description: String? = null,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photo: String? = null
)

@Serializable
data class Follow(
    @SerialName("user_id") val userId: String,
    @SerialName("post_id") val postId: Long
)

@OptIn(ExperimentalTime::class)
@Serializable
data class Message(
    val id: Long = 0,
    @SerialName("user_id") val userId: String,
    @Serializable(with = InstantSerializer::class)
    val sendTime: Instant,
    val message: String,
    @SerialName("chatId") val chatId: Long? = null
)

@Serializable
data class Discarded(
    @SerialName("user_id") val userId: String,
    @SerialName("post_id") val postId: Long
)