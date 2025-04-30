package com.aether.core.runtime.utils

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

object JsonReader {
    fun readJsonFromAssets(context: Context, fileName: String): JSONObject? {
        var json: String? = null
        try {
            val assetManager: AssetManager = context.assets
            val inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        try {
            return JSONObject(json)
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }
}