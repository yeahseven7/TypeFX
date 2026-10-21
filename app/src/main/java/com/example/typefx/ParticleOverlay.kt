package com.example.typefx

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.Random

class ParticleOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Long,
        var ttl: Long,
        var size: Float,
        var color: Int,
        var shape: Int,
        var rotation: Float,
        var rotationSpeed: Float
    )

    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animator: ValueAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    fun trail(pos: PointF) {
        addParticles(pos, 2, 0, 30)
    }

    fun burst(pos: PointF, kind: Int, intensity: Int) {
        val count = (intensity / 8).coerceIn(3, 26)
        addParticles(pos, count, kind, intensity)
    }

    private fun addParticles(pos: PointF, count: Int, kind: Int, intensity: Int) {
        val speed = 2.0f + intensity / 25.0f

        repeat(count) {
            val angle = random.nextDouble() * 2.0 * Math.PI
            val velocity = random.nextFloat() * speed + 1.5f

            val vx = (Math.cos(angle) * velocity).toFloat()
            val vy = (Math.sin(angle) * velocity - 2.0f).toFloat()

            val ttl = 420L + random.nextInt(480).toLong()

            val size = when (kind) {
                1 -> 8f + random.nextInt(10).toFloat()
                2 -> 6f + random.nextInt(7).toFloat()
                else -> 3f + random.nextInt(6).toFloat()
            }

            val color = when (kind) {
                1 -> Color.rgb(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )

                2 -> Color.argb(
                    180,
                    120 + random.nextInt(120),
                    200,
                    255
                )

                else -> Color.rgb(255, 214, 100)
            }

            val shape = when (kind) {
                1 -> 1
                2 -> 2
                else -> 0
            }

            particles.add(
                Particle(
                    x = pos.x,
                    y = pos.y,
                    vx = vx,
                    vy = vy,
                    life = ttl,
                    ttl = ttl,
                    size = size,
                    color = color,
                    shape = shape,
                    rotation = random.nextFloat() * 360f,
                    rotationSpeed = random.nextFloat() * 10f - 5f
                )
            )
        }

        startAnimator()
    }

    private fun startAnimator() {
        if (animator?.isRunning == true) return

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {
                step()
                invalidate()
            }

            start()
        }
    }

    private fun step() {
        if (particles.isEmpty()) {
            animator?.cancel()
            animator = null
            return
        }

        val iterator = particles.iterator()

        while (iterator.hasNext()) {
            val p = iterator.next()

            p.life -= 16

            if (p.life <= 0) {
                iterator.remove()
                continue
            }

            p.x += p.vx
            p.y += p.vy
            p.vy += 0.22f
            p.vx *= 0.98f
            p.rotation += p.rotationSpeed
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (p in particles) {
            val alpha = ((p.life.toFloat() / p.ttl.toFloat()) * 255f)
                .toInt()
                .coerceIn(0, 255)

            paint.color = p.color
            paint.alpha = alpha

            when (p.shape) {
                1 -> {
                    canvas.save()
                    canvas.rotate(p.rotation, p.x, p.y)

                    canvas.drawRect(
                        p.x - p.size,
                        p.y - p.size / 2f,
                        p.x + p.size,
                        p.y + p.size / 2f,
                        paint
                    )

                    canvas.restore()
                }

                2 -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = p.size / 3f

                    canvas.drawCircle(p.x, p.y, p.size, paint)

                    paint.style = Paint.Style.FILL
                }

                else -> {
                    canvas.drawCircle(p.x, p.y, p.size, paint)
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        animator = null
        super.onDetachedFromWindow()
    }
}
