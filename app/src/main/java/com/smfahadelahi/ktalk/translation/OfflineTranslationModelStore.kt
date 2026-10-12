package com.smfahadelahi.ktalk.translation

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

data class Small100ModelFiles(
    val directory: File,
    val encoder: File,
    val decoder: File,
    val tokenizer: File,
    val languageTokens: File
)

/**
 * Installs a user-selected ZIP into app-private storage. The app itself does
 * not download models or require network permission.
 */
object OfflineTranslationModelStore {
    private const val MAX_TOTAL_BYTES = 1_200_000_000L

    fun findInstalled(context: Context): Small100ModelFiles? {
        val dir = File(context.filesDir, "models/small100")
        val encoder = File(dir, "encoder_model.onnx")
        val decoder = File(dir, "decoder_model_merged.onnx")
        val tokenizer = File(dir, "tokenizer.json")
        val languageTokens = File(dir, "lang_tokens.json")
        if (!encoder.isFile || encoder.length() < 100_000_000L) return null
        if (!decoder.isFile || decoder.length() < 150_000_000L) return null
        if (!tokenizer.isFile || tokenizer.length() < 10_000L) return null
        if (!languageTokens.isFile || languageTokens.length() < 100L) return null
        return runCatching {
            val langMap = JSONObject(languageTokens.readText()).getJSONObject("lang_to_id")
            require(langMap.has("ko") && langMap.has("bn"))
            Small100ModelFiles(dir, encoder, decoder, tokenizer, languageTokens)
        }.getOrNull()
    }

    suspend fun installZip(context: Context, uri: Uri): Result<Small100ModelFiles> =
        withContext(Dispatchers.IO) {
            runCatching {
                val root = File(context.filesDir, "models").apply { mkdirs() }
                val staging = File(root, "small100-installing")
                deleteRecursivelySafely(staging)
                check(staging.mkdirs()) { "Could not create model staging directory." }

                try {
                    val seen = mutableSetOf<String>()
                    var totalBytes = 0L
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error("Could not open the selected model ZIP.")

                    ZipInputStream(BufferedInputStream(input)).use { zip ->
                        while (true) {
                            val entry = zip.nextEntry ?: break
                            if (!entry.isDirectory) {
                                val targetName = when {
                                    entry.name == "onnx/encoder_model.onnx" || entry.name.endsWith("/onnx/encoder_model.onnx") -> "encoder_model.onnx"
                                    entry.name == "onnx/decoder_model_merged.onnx" || entry.name.endsWith("/onnx/decoder_model_merged.onnx") -> "decoder_model_merged.onnx"
                                    entry.name == "tokenizer.json" || entry.name.endsWith("/tokenizer.json") -> "tokenizer.json"
                                    entry.name == "lang_tokens.json" || entry.name.endsWith("/lang_tokens.json") -> "lang_tokens.json"
                                    else -> null
                                }
                                if (targetName != null) {
                                    check(seen.add(targetName)) { "The ZIP contains duplicate $targetName entries." }
                                    val target = File(staging, targetName)
                                    BufferedOutputStream(FileOutputStream(target)).use { output ->
                                        val buffer = ByteArray(64 * 1024)
                                        var written = 0L
                                        while (true) {
                                            val count = zip.read(buffer)
                                            if (count < 0) break
                                            written += count
                                            totalBytes += count
                                            check(totalBytes <= MAX_TOTAL_BYTES) { "Model pack is larger than the allowed 1.2 GB." }
                                            output.write(buffer, 0, count)
                                        }
                                        output.flush()
                                    }
                                }
                            }
                            zip.closeEntry()
                        }
                    }

                    val required = setOf(
                        "encoder_model.onnx",
                        "decoder_model_merged.onnx",
                        "tokenizer.json",
                        "lang_tokens.json"
                    )
                    check(seen.containsAll(required)) {
                        "Model ZIP is incomplete. It must include both ONNX files, tokenizer.json, and lang_tokens.json."
                    }

                    val stagedFiles = validateDirectory(staging)
                    val destination = File(root, "small100")
                    deleteRecursivelySafely(destination)
                    check(staging.renameTo(destination)) { "Could not move the installed model into app storage." }
                    checkNotNull(findInstalled(context)) { "Model pack validation failed after installation." }
                } catch (error: Throwable) {
                    deleteRecursivelySafely(staging)
                    throw error
                }
            }
        }

    private fun validateDirectory(dir: File): Small100ModelFiles {
        val encoder = File(dir, "encoder_model.onnx")
        val decoder = File(dir, "decoder_model_merged.onnx")
        val tokenizer = File(dir, "tokenizer.json")
        val languageTokens = File(dir, "lang_tokens.json")
        check(encoder.isFile && encoder.length() >= 100_000_000L) { "Encoder model file is missing or too small." }
        check(decoder.isFile && decoder.length() >= 150_000_000L) { "Decoder model file is missing or too small." }
        check(tokenizer.isFile && tokenizer.length() >= 10_000L) { "Tokenizer file is missing or invalid." }
        check(languageTokens.isFile && languageTokens.length() >= 100L) { "Language-token map is missing." }
        val map = JSONObject(languageTokens.readText()).getJSONObject("lang_to_id")
        check(map.has("ko") && map.has("bn")) { "This model pack does not include Korean and Bengali language tokens." }
        return Small100ModelFiles(dir, encoder, decoder, tokenizer, languageTokens)
    }

    private fun deleteRecursivelySafely(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) file.listFiles()?.forEach(::deleteRecursivelySafely)
        file.delete()
    }
}
