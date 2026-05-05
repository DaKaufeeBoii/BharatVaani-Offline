package com.kaufee.bv.data.manager

import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import org.vosk.Recognizer
import javax.inject.Inject
import javax.inject.Singleton

data class SttState(
    val isListening: Boolean = false,
    val partialResult: String = "",
    val error: String? = null
)

@Singleton
class SttManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelLoader: VoskModelLoader
) {
    companion object {
        private const val SAMPLE_RATE = 16000
    }

    private val _state = MutableStateFlow(SttState())
    val state: StateFlow<SttState> = _state

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun startListening(languageCode: String, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _state.update { it.copy(error = "Microphone permission not granted") }
            return
        }

        job?.cancel()

        job = scope.launch {
            _state.update { it.copy(isListening = true, error = null) }

            var recorder: AudioRecord? = null

            try {
                val model = modelLoader.getModel(languageCode)

                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                val buffer = ShortArray(bufferSize)

                Recognizer(model, SAMPLE_RATE.toFloat()).use { recognizer ->
                    recorder.startRecording()

                    while (_state.value.isListening && isActive) {
                        val read = recorder.read(buffer, 0, buffer.size)

                        if (read > 0) {
                            val bytes = ByteArray(read * 2)
                            for (i in 0 until read) {
                                bytes[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                                bytes[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                            }

                            if (recognizer.acceptWaveForm(bytes, bytes.size)) {
                                val text = parse(recognizer.result)
                                if (text.isNotBlank()) onResult(text)
                            } else {
                                val partial = parsePartial(recognizer.partialResult)
                                _state.update { it.copy(partialResult = partial) }
                            }
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("STT", "Error", e)
                _state.update { it.copy(error = e.message) }
            } finally {
                recorder?.release()
                _state.update { it.copy(isListening = false) }
            }
        }
    }

    fun stopListening() {
        _state.update { it.copy(isListening = false) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun destroy() {
        job?.cancel()
        scope.cancel()
    }

    private fun parse(json: String) =
        try { JSONObject(json).optString("text") } catch (_: Exception) { "" }

    private fun parsePartial(json: String) =
        try { JSONObject(json).optString("partial") } catch (_: Exception) { "" }
}