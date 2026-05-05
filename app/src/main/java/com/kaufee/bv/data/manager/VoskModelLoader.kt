package com.kaufee.bv.data.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.vosk.Model
import java.io.File
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoskModelLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelCache = mutableMapOf<String, Model>()

    private val assetNames = mapOf(
        "en" to "vosk/vosk-model-small-en-us-0.15.zip",
        "hi" to "vosk/vosk-model-small-hi-0.22.zip",
        "te" to "vosk/vosk-model-small-te-0.42.zip"
    )

    private val fallback = mapOf(
        "ta" to "en",
        "mr" to "hi"
    )

    suspend fun getModel(languageCode: String): Model = withContext(Dispatchers.IO) {
        val code = fallback[languageCode] ?: languageCode
        modelCache[code] ?: loadModel(code).also { modelCache[code] = it }
    }

    private fun loadModel(languageCode: String): Model {
        val asset = assetNames[languageCode]
            ?: throw IllegalArgumentException("No model for $languageCode")

        val dir = File(context.filesDir, "vosk/$languageCode")

        if (!dir.exists() || dir.listFiles().isNullOrEmpty()) {
            dir.mkdirs()
            extract(asset, dir)
        }

        val inner = dir.listFiles()?.firstOrNull { it.isDirectory } ?: dir
        return Model(inner.absolutePath)
    }

    private fun extract(assetPath: String, target: File) {
        context.assets.open(assetPath).use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val file = File(target, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        file.outputStream().use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }
    }

    fun releaseAll() {
        modelCache.values.forEach { it.close() }
        modelCache.clear()
    }
}