package com.hhp227.application.helper

import androidx.datastore.core.Serializer
import com.hhp227.application.dto.UserItem
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserItemSerializer : Serializer<UserItem?> {
    override val defaultValue: UserItem?
        get() = null

    override suspend fun readFrom(input: InputStream): UserItem? {
        return try {
            Json.decodeFromString(UserItem.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: UserItem?, output: OutputStream) {
        t?.let { output.write(Json.encodeToString(UserItem.serializer(), it).encodeToByteArray()) }
    }
}