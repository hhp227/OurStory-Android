package com.hhp227.application.helper

import androidx.datastore.core.Serializer
import com.hhp227.application.model.UserPreference
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserSerializer : Serializer<UserPreference> {
    override val defaultValue: UserPreference
        get() = UserPreference(null)

    override suspend fun readFrom(input: InputStream): UserPreference {
        return try {
            Json.decodeFromString(UserPreference.serializer(), input.readBytes().decodeToString())
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreference, output: OutputStream) {
        output.write(Json.encodeToString(UserPreference.serializer(), t).encodeToByteArray())
    }
}