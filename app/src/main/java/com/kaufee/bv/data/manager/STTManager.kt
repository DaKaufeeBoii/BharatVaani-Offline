package com.kaufee.bv.data.manager

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import android.util.Log
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
        private const val TAG = "SttManager"
        private const val SAMPLE_RATE = 16000
    }

    private val _state = MutableStateFlow(SttState())
    val state: StateFlow<SttState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun startListening(languageCode: String, onResult: (String) -> Unit) {
        if (_state.value.isListening) {
            Log.d(TAG, "Already listening, ignoring start request")
            return
        }

        job?.cancel()

        job = scope.launch {
            _state.update { it.copy(isListening = true, partialResult = "", error = null) }

            var recorder: AudioRecord? = null

            try {
                Log.d(TAG, "Loading model for $languageCode")
                val model = modelLoader.getModel(languageCode)

                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ) * 2

                if (bufferSize <= 0) {
                    throw IllegalStateException("AudioRecord min buffer size is invalid")
                }

                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord initialization failed. Check permissions.")
                }

                val buffer = ShortArray(bufferSize / 2)

                Recognizer(model, SAMPLE_RATE.toFloat()).use { recognizer ->
                    recorder.startRecording()
                    Log.d(TAG, "Started recording")

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

                                if (text.isNotBlank()) {
                                    Log.d(TAG, "Result: $text")
                                    onResult(text)
                                    _state.update { it.copy(partialResult = "") }
                                }

                            } else {
                                val partial = parsePartial(recognizer.partialResult)
                                if (partial.isNotBlank()) {
                                    _state.update { it.copy(partialResult = partial) }
                                }
                            }
                        }
                    }

                    val final = parse(recognizer.finalResult)
                    if (final.isNotBlank()) {
                        Log.d(TAG, "Final result: $final")
                        onResult(final)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "STT Error: ${e.message}", e)
                _state.update {
                    it.copy(
                        isListening = false,
                        error = "Voice error: ${e.message}"
                    )
                }
            } finally {
                Log.d(TAG, "Stopping recorder")
                recorder?.apply {
                    try {
                        if (recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            stop()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error stopping recorder: ${e.message}")
                    }
                    release()
                }

                _state.update { it.copy(isListening = false, partialResult = "") }
            }
        }
    }

    fun stopListening() {
        Log.d(TAG, "Stop listening requested")
        _state.update { it.copy(isListening = false) }
    }

    fun destroy() {
        Log.d(TAG, "Destroying SttManager")
        job?.cancel()
        scope.cancel()
        _state.value = SttState()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun parse(json: String): String = try {
        JSONObject(json).optString("text", "").trim()
    } catch (_: Exception) {
        ""
    }

    private fun parsePartial(json: String): String = try {
        JSONObject(json).optString("partial", "").trim()
    } catch (_: Exception) {
        ""
    }
}
