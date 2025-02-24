package com.hhp227.application.helper

import androidx.datastore.core.Serializer
import com.hhp227.application.model.UserPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences
        get() = UserPreferences(null)

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            Json.decodeFromString(UserPreferences.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        output.write(Json.encodeToString(UserPreferences.serializer(), t).encodeToByteArray())
    }
}