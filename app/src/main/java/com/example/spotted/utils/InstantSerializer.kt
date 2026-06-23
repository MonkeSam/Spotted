package com.example.spotted.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString()
        // Supabase restituisce "2024-01-15T18:30:00" senza fuso orario,
        // Instant.parse() richiede la Z finale per interpretarlo come UTC
        val normalized = if (raw.endsWith("Z") || raw.contains("+")) raw else "${raw}Z"
        return Instant.parse(normalized)
    }
}