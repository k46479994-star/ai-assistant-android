package com.example.aiassistant.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class VoiceInputController(
    private val activity: Activity,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null

    fun start() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
            onError("마이크 권한을 허용한 뒤 다시 눌러 주세요")
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            onError("이 기기에서는 음성 인식을 사용할 수 없습니다")
            return
        }

        destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onError(error: Int) {
                    onError(errorMessage(error))
                    destroy()
                }

                override fun onResults(results: Bundle?) {
                    val text = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (text.isBlank()) onError("음성을 인식하지 못했습니다. 다시 시도해 주세요")
                    else onResult(text)
                    destroy()
                }
            })
            startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "일정, 할 일 또는 메모를 말씀해 주세요")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            })
        }
    }

    fun destroy() {
        recognizer?.cancel()
        recognizer?.destroy()
        recognizer = null
    }

    private fun errorMessage(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH -> "음성을 인식하지 못했습니다. 다시 시도해 주세요"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말씀을 듣지 못했습니다. 다시 눌러 주세요"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "현재 음성 인식 서비스를 사용할 수 없습니다"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "마이크 권한이 필요합니다"
        else -> "음성 입력을 시작하지 못했습니다"
    }

    companion object {
        const val REQUEST_RECORD_AUDIO = 4102
    }
}
