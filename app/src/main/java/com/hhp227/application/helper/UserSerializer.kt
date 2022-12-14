package com.hhp227.application.helper

import androidx.datastore.core.Serializer
import com.hhp227.application.model.User
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserSerializer : Serializer<User?> {
    override val defaultValue: User?
        get() = null

    override suspend fun readFrom(input: InputStream): User? {
        return try {
            Json.decodeFromString(User.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: User?, output: OutputStream) {
        t?.let { output.write(Json.encodeToString(User.serializer(), it).encodeToByteArray()) }
    }
}