package com.example.api3

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class SecondActivity : AppCompatActivity() {
    private lateinit var elevenLabs: ElevenLabs // ElevenLabs 인스턴스 초기화 필요

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        elevenLabs = ElevenLabs("b1c93bebf716c4991f49ffeaad2ce488")

        val speakButton: Button = findViewById(R.id.speakButton)
        speakButton.setOnClickListener {
            val text = "Hello, my name is sohi."
            val voice = "EXAVITQu4vr4xnSDxMaL"
            elevenLabs.textToSpeech(text, voice) { audioData ->
                audioData?.let {
                    playAudio(it)
                }
            }
        }
    }

    private fun playAudio(audioData: ByteArray) {
        val tempFile = File.createTempFile("audio", "mp3", cacheDir)
        FileOutputStream(tempFile).use { output ->
            output.write(audioData)
        }
        MediaPlayer().apply {
            setDataSource(tempFile.path)
            prepare()
            start()
        }
    }
}
