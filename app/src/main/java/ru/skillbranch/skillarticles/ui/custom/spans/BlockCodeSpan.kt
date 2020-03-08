package ru.skillbranch.skillarticles.ui.custom.spans

import android.graphics.*
import android.text.style.ReplacementSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import ru.skillbranch.skillarticles.data.repositories.Element


class BlockCodeSpan(
    @ColorInt private val textColor: Int,
    @ColorInt private val bgColor: Int,
    @Px private val cornerRadius: Float,
    @Px private val padding: Float,
    private val type: Element.BlockCode.Type
) : ReplacementSpan() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var rect = RectF()
    @VisibleForTesting
    var path = Path()

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        when (type) {
            Element.BlockCode.Type.START -> {
                paint.forBackground {
                    val corners = floatArrayOf(
                        cornerRadius, cornerRadius,
                        cornerRadius, cornerRadius,
                        0f, 0f,
                        0f, 0f
                    )
                    rect.set(0f, top + padding, canvas.width.toFloat(), bottom.toFloat())
                    path.reset()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }
                paint.forText {
                    canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
                }
            }
            Element.BlockCode.Type.END -> {
                paint.forBackground {
                    val corners = floatArrayOf(
                        0f, 0f,   // Top left radius in px
                        0f, 0f,   // Top right radius in px
                        cornerRadius, cornerRadius,     // Bottom right radius in px
                        cornerRadius, cornerRadius      // Bottom left radius in px
                    )

                    rect.set(
                        0f,
                        top.toFloat(),
                        canvas.width.toFloat(),
                        bottom - padding
                    )
                    path.reset()
                    path.addRoundRect(rect, corners, Path.Direction.CW)
                    canvas.drawPath(path, paint)
                }
                paint.forText {
                    canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
                }
            }
            Element.BlockCode.Type.MIDDLE -> {
                paint.forBackground {
                    rect.set(
                        0f,
                        top.toFloat(),
                        canvas.width.toFloat(),
                        bottom.toFloat()
                    )
                    canvas.drawRect(rect, paint)
                }
                paint.forText {
                    canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
                }
            }
            Element.BlockCode.Type.SINGLE -> {
                paint.forBackground {
                    rect.set(
                        0f,
                        top + padding,
                        canvas.width.toFloat(),
                        bottom - padding
                    )
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                }
                paint.forText {
                    canvas.drawText(text, start, end, x + padding, y.toFloat(), paint)
                }
            }
        }
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        paint.forText {
            if (fm != null) {
                when (type) {
                    Element.BlockCode.Type.START -> {
                        fm.ascent = (paint.ascent() - 2 * padding).toInt()
                        fm.descent = paint.descent().toInt()
                    }

                    Element.BlockCode.Type.END -> {
                        fm.ascent = paint.ascent().toInt()
                        fm.descent = (paint.descent() + 2 * padding).toInt()
                    }

                    Element.BlockCode.Type.MIDDLE -> {
                        fm.ascent = paint.ascent().toInt()
                        fm.descent = paint.descent().toInt()
                    }

                    Element.BlockCode.Type.SINGLE -> {
                        fm.ascent = (paint.ascent() - 2 * padding).toInt()
                        fm.descent = (paint.descent() + 2 * padding).toInt()
                    }
                }
            }
        }
        return 0
    }

    private inline fun Paint.forBackground(block: () -> Unit) {
        val oldStyle = style
        val oldColor = color

        color = bgColor
        style = Paint.Style.FILL

        block()

        color = oldColor
        style = oldStyle
    }

    private inline fun Paint.forText(block: () -> Unit) {
        val oldStyle = typeface?.style ?: 0
        val oldTypeface = typeface
        val oldSize = textSize
        val oldColor = color

        typeface = Typeface.create(Typeface.MONOSPACE, oldStyle)
        textSize *= 0.85f
        color = textColor

        block()

        typeface = oldTypeface
        textSize = oldSize
        color = oldColor

    }
}