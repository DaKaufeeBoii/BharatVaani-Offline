package com.kaufee.bv.data.manager

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    val state: StateFlow<SttState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    fun startListening(languageCode: String, onResult: (String) -> Unit) {
        if (_state.value.isListening) return

        job?.cancel()

        job = scope.launch {
            _state.value = SttState(isListening = true)

            try {
                val model = modelLoader.getModel(languageCode)

                val bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ) * 2

                val recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                val buffer = ShortArray(bufferSize / 2)

                Recognizer(model, SAMPLE_RATE.toFloat()).use { recognizer ->
                    recorder.startRecording()

                    while (_state.value.isListening) {
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
                                    recorder.stop()
                                    recorder.release()
                                    _state.value = SttState()
                                    onResult(text)
                                    return@launch
                                }
                            } else {
                                val partial = parsePartial(recognizer.partialResult)
                                if (partial.isNotBlank()) {
                                    _state.value = _state.value.copy(partialResult = partial)
                                }
                            }
                        }
                    }

                    val final = parse(recognizer.finalResult)
                    recorder.stop()
                    recorder.release()
                    _state.value = SttState()

                    if (final.isNotBlank()) onResult(final)
                }

            } catch (e: Exception) {
                _state.value = SttState(
                    error = "Voice error: ${e.message}"
                )
            }
        }
    }

    fun stopListening() {
        _state.value = _state.value.copy(isListening = false)
    }

    fun destroy() {
        job?.cancel()
        _state.value = SttState()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun parse(json: String): String =
        Regex("\"text\"\\s*:\\s*\"([^\"]*)\"")
            .find(json)?.groupValues?.get(1)?.trim() ?: ""

    private fun parsePartial(json: String): String =
        Regex("\"partial\"\\s*:\\s*\"([^\"]*)\"")
            .find(json)?.groupValues?.get(1)?.trim() ?: ""
}