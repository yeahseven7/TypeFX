package com.example.typefx

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PointF
import android.media.AudioManager
import android.os.HapticFeedbackConstants
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Random

class EffectsController(
    private val activity: Activity,
    private val host: FrameLayout,
    private val overlay: ParticleOverlay
) {

    var source: View? = null

    private val prefs: SharedPreferences =
        activity.getSharedPreferences("typefx", Context.MODE_PRIVATE)

    private val random = Random()

    private val audio by lazy {
        activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private var streak = 0
    private var lastKeyTime = 0L

    fun onCharTyped(ch: Char, pos: PointF?) {
        val now = SystemClock.uptimeMillis()

        streak = if (now - lastKeyTime < 650L) streak + 1 else 1
        lastKeyTime = now

        val p = toHostCoordinates(pos)

        spawnChar(ch, p, false)

        if (prefs.getBoolean("cursor_trail", true) && p != null) {
            overlay.trail(p)
        }

        val particleMode = prefs.getInt("particle_style", 1)

        if (particleMode != 0 && p != null) {
            overlay.burst(
                p,
                particleMode - 1,
                prefs.getInt("intensity", 60)
            )
        }

        feedback(false)
        streakPulse()
    }

    fun onCharDeleted(ch: Char, pos: PointF?) {
        streak = 0

        val p = toHostCoordinates(pos)

        spawnChar(ch, p, true)

        val particleMode = prefs.getInt("particle_style", 1)

        if (particleMode != 0 && p != null) {
            overlay.burst(
                p,
                particleMode - 1,
                prefs.getInt("intensity", 60) / 2
            )
        }

        feedback(true)
    }

    private fun toHostCoordinates(pos: PointF?): PointF? {
        pos ?: return null

        val src = source ?: return pos

        val srcLoc = IntArray(2)
        src.getLocationInWindow(srcLoc)

        val hostLoc = IntArray(2)
        host.getLocationInWindow(hostLoc)

        return PointF(
            pos.x + srcLoc[0] - hostLoc[0],
            pos.y + srcLoc[1] - hostLoc[1]
        )
    }

    private fun feedback(delete: Boolean) {
        if (prefs.getBoolean("haptics", true)) {
            source?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }

        if (prefs.getBoolean("sound", false)) {
            audio.playSoundEffect(
                if (delete) AudioManager.FX_KEYPRESS_DELETE
                else AudioManager.FX_KEYPRESS_STANDARD,
                0.25f
            )
        }
    }

    private fun streakPulse() {
        if (!prefs.getBoolean("streak_glow", true)) return

        val view = source ?: return

        if (streak > 0 && streak % 14 == 0) {
            view.animate().cancel()

            view.animate()
                .scaleX(1.015f)
                .scaleY(1.015f)
                .setDuration(70)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(140)
                }
                .start()
        }
    }

    private fun spawnChar(ch: Char, pos: PointF?, delete: Boolean) {
        if (ch == '\n') return

        val display = if (ch == ' ') "·" else ch.toString()

        val view = TextView(activity)
        view.text = display
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        view.setTextColor(ContextCompat.getColor(activity, R.color.fx_char))

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        view.layoutParams = layoutParams

        view.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        val width = view.measuredWidth.toFloat()
        val height = view.measuredHeight.toFloat()

        host.addView(view)

        val baseX = pos?.x ?: (host.width / 2f)
        val baseY = pos?.y ?: (host.height / 2f)

        view.x = baseX - width / 2f
        view.y = baseY - height - dp(8f)
        view.alpha = 0f

        val mode = if (delete) {
            prefs.getInt("delete_style", 0)
        } else {
            prefs.getInt("type_style", 0)
        }

        val intensity = prefs.getInt("intensity", 60)

        val duration = (180L + (100 - intensity) * 3L).coerceIn(120L, 600L)

        if (!delete) {
            when (mode) {
                0 -> {
                    // Pop
                    view.alpha = 1f
                    view.scaleX = 0.25f
                    view.scaleY = 0.25f

                    view.animate()
                        .alpha(0f)
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .translationYBy(-dp(26f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                1 -> {
                    // Slide
                    view.alpha = 1f
                    view.translationX = -dp(16f)

                    view.animate()
                        .alpha(0f)
                        .translationXBy(dp(30f))
                        .translationYBy(-dp(16f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                2 -> {
                    // Fade
                    view.alpha = 1f

                    view.animate()
                        .alpha(0f)
                        .translationYBy(-dp(20f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                3 -> {
                    // Bounce
                    view.alpha = 1f
                    view.scaleX = 0.7f
                    view.scaleY = 0.4f

                    view.animate()
                        .alpha(0f)
                        .scaleX(1.25f)
                        .scaleY(1.25f)
                        .translationYBy(-dp(36f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                else -> {
                    // Glitch
                    view.alpha = 1f
                    view.rotation = if (random.nextBoolean()) -8f else 8f

                    view.animate()
                        .alpha(0f)
                        .translationXBy(if (random.nextBoolean()) dp(24f) else -dp(24f))
                        .translationYBy(-dp(12f))
                        .rotationBy(if (random.nextBoolean()) 20f else -20f)
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }
            }
        } else {
            when (mode) {
                0 -> {
                    // Shrink
                    view.alpha = 1f
                    view.scaleX = 1.2f
                    view.scaleY = 1.2f

                    view.animate()
                        .alpha(0f)
                        .scaleX(0.1f)
                        .scaleY(0.1f)
                        .translationYBy(dp(8f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                1 -> {
                    // Fall
                    view.alpha = 1f

                    view.animate()
                        .alpha(0f)
                        .translationYBy(dp(54f))
                        .rotationBy(26f)
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                2 -> {
                    // Explode
                    view.alpha = 1f

                    view.animate()
                        .alpha(0f)
                        .scaleX(2.4f)
                        .scaleY(2.4f)
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }

                else -> {
                    // Slide left
                    view.alpha = 1f

                    view.animate()
                        .alpha(0f)
                        .translationXBy(-dp(60f))
                        .translationYBy(dp(6f))
                        .setDuration(duration)
                        .setListener(removeListener(view))
                        .start()
                }
            }
        }
    }

    private fun removeListener(view: View): AnimatorListenerAdapter {
        return object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                host.removeView(view)
            }
        }
    }

    private fun dp(value: Float): Float {
        return value * activity.resources.displayMetrics.density
    }
}
