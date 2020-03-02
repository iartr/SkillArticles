package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px

class BlockquotesSpan(
    @Px
    private val gapWidth: Float,
    @Px
    private val quoteWidth: Float,
    @ColorInt
    private val lineColor: Int
) : LeadingMarginSpan {

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, currentMarginLocation: Int, paragraphDirection: Int,
        lineTop: Int, lineBaseline: Int, lineBottom: Int, text: CharSequence?, lineStart: Int,
        lineEnd: Int, isFirstLine: Boolean, layout: Layout?
    ) {
        paint.withCustomColor {
            canvas.drawLine(
                quoteWidth/2f,
                lineTop.toFloat(),
                quoteWidth/2f,
                lineBottom.toFloat(),
                paint
            )
        }
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return (quoteWidth + gapWidth).toInt()
    }

    private inline fun Paint.withCustomColor(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style
        val oldWidth = strokeWidth
        color = lineColor
        style = Paint.Style.STROKE // просто линия
        strokeWidth = quoteWidth
        block()
        // Восстановим старый цвет - чтобы bullet цветом не продолжил рисовать прочие элементы
        strokeWidth = oldWidth
        color = oldColor
        style = oldStyle
    }
}