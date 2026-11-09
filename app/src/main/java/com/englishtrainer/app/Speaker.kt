package com.englishtrainer.app

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

enum class Accent(val label: String, val locale: Locale) {
    US("Американское", Locale.US),
    UK("Британское", Locale.UK)
}

/** Озвучка слов встроенным движком Android TextToSpeech (работает офлайн, если установлены голоса). */
class Speaker(context: Context) {
    private var ready = false
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        ready = status == TextToSpeech.SUCCESS
    }

    fun speak(text: String, accent: Accent) {
        if (!ready) return
        tts.language = accent.locale
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word")
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
