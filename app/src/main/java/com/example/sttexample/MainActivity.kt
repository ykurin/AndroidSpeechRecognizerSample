package com.example.sttexample

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 100
private const val STT_ACTIVITY_RESULT_CODE = 200

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer

    private var sttInProgress = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(SpeechRecognizerListener())
        } else {
            textView.setText(R.string.recognition_is_not_available)
            button.visibility = View.GONE
        }
    }

    fun onRunSTTClick(view: View) {
        if (!sttInProgress) {
            checkPermissions()
        } else {
            stopSTT()
        }
    }

    private fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSTT()
        } else {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (0 < grantResults.size) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSTT()
                } else {
                    Toast.makeText(this, "Audio recording permissions denied.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun stopSTT() {
        speechRecognizer.stopListening()
        sttInProgress = false
        stopSTTUpdateUI()
    }

    private fun stopSTTUpdateUI() {
        sttInProgress = false
        button.setText(R.string.start_stt)
    }

    private fun startSTT() {

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)


        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

        // This flag is deprecated on Android 10. There is no any documentation related to this issue.
        //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)

        // Use this to start default Android Speech Recognition activity and parse the result in onActivityResult
        //startActivityForResult(intent, STT_ACTIVITY_RESULT_CODE)

        speechRecognizer.startListening(intent)

        sttInProgress = true
        button.setText(R.string.stop_stt)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == STT_ACTIVITY_RESULT_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            var str = ""
            if (resultList != null) {
                for (result in resultList) {
                    str += result
                }
            } else {
                str += "Empty result"
            }

            textView.text = str
            stopSTTUpdateUI()

        }
    }

    inner class SpeechRecognizerListener : RecognitionListener {
        private val TAG = "RecognizerListener"

        override fun onReadyForSpeech(params: Bundle) {
            Log.d(TAG, "onReadyForSpeech")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged: sound level changed to $rmsdB dB")
        }

        override fun onBufferReceived(buffer: ByteArray) {
            Log.d(TAG, "onBufferReceived")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndofSpeech")
        }

        override fun onError(error: Int) {
            stopSTTUpdateUI()

            val errorText = "error code " + error + " : " + getErrorText(error)
            Log.d(TAG, errorText)
            textView.text = errorText
        }

        override fun onResults(results: Bundle) {
            stopSTTUpdateUI()

            var str = ""
            Log.d(TAG, "onResults $results")
            val resultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (resultList != null) {
//                for (result in resultList) {
//                    str += result
//                }
                str += resultList[0]
            } else {
                str += "Empty result"
            }

            textView.text = str
        }

        override fun onPartialResults(partialResults: Bundle) {
            Log.d(TAG, "onPartialResults")

            var str = ""
            val resultList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (resultList != null) {
                for (result in resultList) {
                    str += result
                }
            } else {
                str += "Empty result"
            }

            textView.text = str
        }

        override fun onEvent(eventType: Int, params: Bundle) {
            Log.d(TAG, "onEvent $eventType")
        }

        fun getErrorText(errorCode: Int): String {
            val message: String
            when (errorCode) {
                SpeechRecognizer.ERROR_AUDIO -> message = getString(R.string.audio_recording_error)
                SpeechRecognizer.ERROR_CLIENT -> message = getString(R.string.client_side_error)
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = getString(R.string.insufficient_permissions)
                SpeechRecognizer.ERROR_NETWORK -> message = getString(R.string.network_error)
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = getString(R.string.network_timeout)
                SpeechRecognizer.ERROR_NO_MATCH -> message = getString(R.string.no_match)
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = getString(R.string.recognition_service_busy)
                SpeechRecognizer.ERROR_SERVER -> message = getString(R.string.error_from_server)
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = getString(R.string.no_speech_input)
                else -> message = getString(R.string.default_speech_input)
            }
            return message
        }
    }
}