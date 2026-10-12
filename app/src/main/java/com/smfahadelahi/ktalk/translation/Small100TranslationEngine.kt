package com.smfahadelahi.ktalk.translation

import android.content.Context
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.LongBuffer

/**
 * Local SMaLL-100 int8 ONNX translation engine.
 *
 * Model files are imported by OfflineTranslationModelStore. Inference runs on
 * device; this class contains no networking or remote API calls.
 */
class Small100TranslationEngine(context: Context) : TranslationEngine, AutoCloseable {
    private val appContext = context.applicationContext
    @Volatile private var translator: Small100OnnxTranslator? = null

    override suspend fun status(): TranslationStatus =
        if (OfflineTranslationModelStore.findInstalled(appContext) != null) {
            TranslationStatus.Ready("SMaLL-100 int8 ONNX (local Korean↔Bengali)")
        } else {
            TranslationStatus.ModelMissing("Import the offline SMaLL-100 model ZIP to enable general Korean↔Bengali text translation.")
        }

    override suspend fun translate(
        text: String,
        direction: TranslationDirection
    ): Result<TranslationResult> = withContext(Dispatchers.Default) {
        runCatching {
            require(text.isNotBlank()) { "Enter text to translate." }
            val local = getTranslator()
                ?: error("Offline translation model is not installed. Import the model ZIP first.")
            val targetLanguage = when (direction) {
                TranslationDirection.KOREAN_TO_BANGLA -> "bn"
                TranslationDirection.BANGLA_TO_KOREAN -> "ko"
            }
            val translated = local.translate(text.trim(), targetLanguage)
            check(translated.isNotBlank()) { "The local model returned an empty translation." }
            TranslationResult(text, translated, direction)
        }
    }

    @Synchronized
    private fun getTranslator(): Small100OnnxTranslator? {
        translator?.let { return it }
        val files = OfflineTranslationModelStore.findInstalled(appContext) ?: return null
        return Small100OnnxTranslator(files).also { translator = it }
    }

    override fun close() {
        synchronized(this) {
            translator?.close()
            translator = null
        }
    }
}

private class Small100OnnxTranslator(
    files: Small100ModelFiles
) : AutoCloseable {
    private val environment = OrtEnvironment.getEnvironment()
    private val options = OrtSession.SessionOptions().apply {
        setIntraOpNumThreads(2)
    }
    private val encoder = environment.createSession(files.encoder.absolutePath, options)
    private val decoder = environment.createSession(files.decoder.absolutePath, options)
    private val tokenizer = HuggingFaceTokenizer.newInstance(files.tokenizer.inputStream(), null)
    private val languageIds: Map<String, Long> = run {
        val languageObject = JSONObject(files.languageTokens.readText()).getJSONObject("lang_to_id")
        val result = mutableMapOf<String, Long>()
        val keys = languageObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = languageObject.getLong(key)
        }
        result
    }

    private val layers = 3
    private val heads = 16L
    private val headDimension = 64L
    private val endToken = 2L
    private val decoderStartToken = 2L

    @Synchronized
    fun translate(text: String, targetLanguage: String, maxLength: Int = 96): String {
        require(languageIds.containsKey(targetLanguage)) { "Unsupported target language: $targetLanguage" }
        val encoded = tokenizer.encode(text).ids
        val inputIds = LongArray(encoded.size + 1)
        inputIds[0] = languageIds.getValue(targetLanguage)
        encoded.copyInto(inputIds, destinationOffset = 1)
        val attentionMask = LongArray(inputIds.size) { 1L }

        val inputTensor = long2d(inputIds)
        val maskTensor = long2d(attentionMask)
        val encoderResult = encoder.run(
            mapOf("input_ids" to inputTensor, "attention_mask" to maskTensor)
        )
        val encoderHidden = encoderResult[0] as OnnxTensor
        var past = emptyPast()
        val generated = ArrayList<Long>()
        var currentToken = decoderStartToken
        var useCache = false

        try {
            for (step in 0 until maxLength) {
                val currentIds = long2d(longArrayOf(currentToken))
                val cacheFlag = OnnxTensor.createTensor(
                    environment,
                    ByteBuffer.wrap(byteArrayOf(if (useCache) 1.toByte() else 0.toByte())),
                    longArrayOf(1),
                    OnnxJavaType.BOOL
                )
                val feeds = HashMap<String, OnnxTensor>()
                feeds["encoder_attention_mask"] = maskTensor
                feeds["input_ids"] = currentIds
                feeds["encoder_hidden_states"] = encoderHidden
                feeds["use_cache_branch"] = cacheFlag
                past.forEach { (name, tensor) -> feeds[name] = tensor }

                val result = try {
                    decoder.run(feeds)
                } finally {
                    currentIds.close()
                    cacheFlag.close()
                }

                val logits = result[0] as OnnxTensor
                val nextToken = argmaxLast(logits)
                val updatedPast = try {
                    rollPast(result)
                } finally {
                    result.close()
                }
                past.values.forEach { runCatching { it.close() } }
                past = updatedPast

                if (nextToken == endToken) break
                generated.add(nextToken)
                currentToken = nextToken
                useCache = true
            }
            return tokenizer.decode(generated.toLongArray(), true)
        } finally {
            past.values.forEach { runCatching { it.close() } }
            runCatching { encoderResult.close() }
            runCatching { inputTensor.close() }
            runCatching { maskTensor.close() }
        }
    }

    private fun long2d(values: LongArray): OnnxTensor =
        OnnxTensor.createTensor(
            environment,
            LongBuffer.wrap(values),
            longArrayOf(1, values.size.toLong())
        )

    private fun emptyPast(): Map<String, OnnxTensor> {
        val tensors = HashMap<String, OnnxTensor>()
        for (layer in 0 until layers) {
            for (kind in listOf("decoder", "encoder")) {
                for (keyValue in listOf("key", "value")) {
                    tensors["past_key_values.$layer.$kind.$keyValue"] =
                        OnnxTensor.createTensor(
                            environment,
                            FloatBuffer.allocate(0),
                            longArrayOf(1, heads, 0, headDimension)
                        )
                }
            }
        }
        return tensors
    }

    private fun rollPast(result: OrtSession.Result): Map<String, OnnxTensor> {
        val names = decoder.outputNames.toList()
        val updated = HashMap<String, OnnxTensor>()
        for (layer in 0 until layers) {
            for (kind in listOf("decoder", "encoder")) {
                for (keyValue in listOf("key", "value")) {
                    val outputName = "present.$layer.$kind.$keyValue"
                    val index = names.indexOf(outputName)
                    check(index >= 0) { "The ONNX decoder output $outputName is missing." }
                    val source = result[index] as OnnxTensor
                    updated["past_key_values.$layer.$kind.$keyValue"] = cloneFloatTensor(source)
                }
            }
        }
        return updated
    }

    private fun cloneFloatTensor(source: OnnxTensor): OnnxTensor {
        val shape = source.info.shape
        val elementCount = shape.fold(1L) { acc, dimension -> acc * dimension }.toInt()
        val sourceBuffer = source.floatBuffer
        val values = FloatArray(elementCount) { index -> sourceBuffer.get(index) }
        return OnnxTensor.createTensor(environment, FloatBuffer.wrap(values), shape)
    }

    private fun argmaxLast(logits: OnnxTensor): Long {
        val shape = logits.info.shape
        require(shape.size == 3) { "Unexpected decoder logits shape." }
        val vocabularySize = shape[2].toInt()
        val start = (shape[1].toInt() - 1) * vocabularySize
        val buffer = logits.floatBuffer
        var bestIndex = 0
        var bestValue = buffer.get(start)
        for (index in 1 until vocabularySize) {
            val value = buffer.get(start + index)
            if (value > bestValue) {
                bestValue = value
                bestIndex = index
            }
        }
        return bestIndex.toLong()
    }

    override fun close() {
        runCatching { tokenizer.close() }
        runCatching { encoder.close() }
        runCatching { decoder.close() }
        runCatching { options.close() }
    }
}
