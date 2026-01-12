package com.smartethnet.rustun.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.smartethnet.rustun.proto.ConfigList
import java.io.InputStream
import java.io.OutputStream

object ConfigDataStore : Serializer<ConfigList> {
    override val defaultValue: ConfigList = ConfigList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ConfigList {
        try {
            return ConfigList.parseFrom(input)
        } catch (exception: IOException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ConfigList,
        output: OutputStream
    ) {
        t.writeTo(output)
    }
}

val Context.configsDataStore: DataStore<ConfigList> by dataStore(
    fileName = "configs.pb",
    serializer = ConfigDataStore
)