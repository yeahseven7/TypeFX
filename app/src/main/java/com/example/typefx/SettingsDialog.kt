package com.example.typefx

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsDialog(
    private val context: Context,
    private val prefs: SharedPreferences
) {

    fun show() {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_settings, null)

        val spinType = view.findViewById<Spinner>(R.id.spinType)
        val spinDelete = view.findViewById<Spinner>(R.id.spinDelete)
        val spinParticles = view.findViewById<Spinner>(R.id.spinParticles)
        val seekIntensity = view.findViewById<SeekBar>(R.id.seekIntensity)

        val switchHaptics = view.findViewById<SwitchMaterial>(R.id.switchHaptics)
        val switchSound = view.findViewById<SwitchMaterial>(R.id.switchSound)
        val switchCursorTrail = view.findViewById<SwitchMaterial>(R.id.switchCursorTrail)
        val switchStreakGlow = view.findViewById<SwitchMaterial>(R.id.switchStreakGlow)

        spinType.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            context.resources.getStringArray(R.array.type_animations)
        )

        spinDelete.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            context.resources.getStringArray(R.array.delete_animations)
        )

        spinParticles.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            context.resources.getStringArray(R.array.particle_effects)
        )

        spinType.setSelection(prefs.getInt("type_style", 0))
        spinDelete.setSelection(prefs.getInt("delete_style", 0))
        spinParticles.setSelection(prefs.getInt("particle_style", 1))

        seekIntensity.progress = prefs.getInt("intensity", 60)

        switchHaptics.isChecked = prefs.getBoolean("haptics", true)
        switchSound.isChecked = prefs.getBoolean("sound", false)
        switchCursorTrail.isChecked = prefs.getBoolean("cursor_trail", true)
        switchStreakGlow.isChecked = prefs.getBoolean("streak_glow", true)

        AlertDialog.Builder(context)
            .setTitle("TypeFX Settings")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                prefs.edit()
                    .putInt("type_style", spinType.selectedItemPosition)
                    .putInt("delete_style", spinDelete.selectedItemPosition)
                    .putInt("particle_style", spinParticles.selectedItemPosition)
                    .putInt("intensity", seekIntensity.progress)
                    .putBoolean("haptics", switchHaptics.isChecked)
                    .putBoolean("sound", switchSound.isChecked)
                    .putBoolean("cursor_trail", switchCursorTrail.isChecked)
                    .putBoolean("streak_glow", switchStreakGlow.isChecked)
                    .apply()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
