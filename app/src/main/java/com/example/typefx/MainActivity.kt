package com.example.typefx

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var fx: EffectsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("typefx", MODE_PRIVATE)

        val host = findViewById<FrameLayout>(R.id.fxHost)
        val overlay = findViewById<ParticleOverlay>(R.id.particleOverlay)
        val editor = findViewById<AnimatedEditText>(R.id.animatedEdit)

        fx = EffectsController(this, host, overlay)
        editor.attachEffects(fx)

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            SettingsDialog(this, prefs).show()
        }
    }
}
