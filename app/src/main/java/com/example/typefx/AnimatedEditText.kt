package com.example.typefx

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class AnimatedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private var effects: EffectsController? = null
    private var lastText: String = ""

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastText = s?.toString().orEmpty()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val fx = effects ?: return
                val newText = s?.toString().orEmpty()
                val oldText = lastText

                if (count > before) {
                    val inserted = count - before

                    for (i in 0 until inserted) {
                        val index = start + i
                        val ch = newText.getOrNull(index) ?: continue

                        post {
                            fx.onCharTyped(ch, cursorPoint(index + 1))
                        }
                    }
                } else if (before > count) {
                    val deleted = before - count

                    for (i in 0 until deleted) {
                        val index = start + i
                        val ch = oldText.getOrNull(index) ?: continue

                        post {
                            fx.onCharDeleted(ch, cursorPoint(start))
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                lastText = s?.toString().orEmpty()
            }
        })
    }

    fun attachEffects(controller: EffectsController) {
        effects = controller
        controller.source = this
    }

    private fun cursorPoint(offset: Int): PointF? {
        val currentText = text ?: return null
        val safeOffset = offset.coerceIn(0, currentText.length)
        val layout = layout ?: return null

        val line = layout.getLineForOffset(safeOffset)
        val bounds = Rect()
        layout.getLineBounds(line, bounds)

        val x = bounds.left + layout.getPrimaryHorizontal(safeOffset) + getTotalPaddingLeft() - scrollX
        val y = bounds.top + getTotalPaddingTop() - scrollY

        return PointF(x.toFloat(), y.toFloat())
    }
}
