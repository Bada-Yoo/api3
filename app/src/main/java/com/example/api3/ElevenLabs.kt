package com.example.api3

import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlinx.serialization.SerialName
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString



class ElevenLabs(val apiKey: String) {

    companion object {
        const val TAG = "ElevenLabs"
    }
    private var jsonBuilder = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
    }

    private val voices = mutableListOf<ElevenLabsVoice>()

    fun speechVoices(language: String, listener: (List<ElevenLabsVoice>) -> Unit) {

        if (voices.isNotEmpty()) {
            listener(voices)
            return
        }

        val url = "https://api.elevenlabs.io/v1/voices"
        val httpAsync = url.httpGet().apply {
            header("Accept", "application/json")
            header("xi-api-key", apiKey)
        }
        Log.i(TAG, "ElevenLabs voices request")
        val start = System.currentTimeMillis()
        httpAsync.responseString { request, response, res ->
            val end = System.currentTimeMillis()
            Log.i(TAG, "ElevenLabs voices request done in ${end - start}ms")
            try {
                val json = res.get()
                val list = jsonBuilder.decodeFromString<ElevenLabsVoicesResponse>(json)
                MainScope().launch {
                    list.voices?.let {
                        voices.clear()
                        voices.addAll(it)
                        listener(voices)
                    }
                }
            }catch (e: Exception) {
                Log.e(TAG, "ElevenLabs speak user", e)
                MainScope().launch {
                    listener(mutableListOf())
                }
            }
        }
    }

    fun getRemoteConfigLong(key: String): Long {
        // 임시로 0을 반환하거나, 적절한 기본값을 설정해야한다!
        return 0L
    }

    fun textToSpeech(text: String, voice: String, listener: (ByteArray?) -> Unit) {
        val optimizeStreamingLatency = getRemoteConfigLong("optimize_streaming_latency")
        val url = "https://api.elevenlabs.io/v1/text-to-speech/$voice?optimize_streaming_latency=$optimizeStreamingLatency"
        val httpAsync = url.httpPost().apply {
            header("Content-Type", "application/json")
            header("Accept", "audio/mpeg")
            header("xi-api-key", apiKey)
        }
        httpAsync.body("{\"text\": \"$text\", \"model_id\": \"eleven_monolingual_v1\"}")
        Log.i(TAG, "ElevenLabs speak request: $text")
        httpAsync.response { _, _, res ->
            Log.i(TAG, "ElevenLabs speak request done")
            try {
                listener(res.get())
            }catch (e: Exception) {
                Log.e(TAG, "ElevenLabs speak error", e)
                MainScope().launch {
                    listener(null)
                }
            }
        }
    }
}

@Serializable
class ElevenLabsVoicesResponse(var voices: List<ElevenLabsVoice>?)
@Serializable
class ElevenLabsVoice(@SerialName("voice_id") val voiceId: String, @SerialName("name")val voiceName: String)