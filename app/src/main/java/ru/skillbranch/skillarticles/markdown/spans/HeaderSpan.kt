package ru.skillbranch.skillarticles.markdown.spans

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.Spanned
import android.text.TextPaint
import android.text.style.LeadingMarginSpan
import android.text.style.LineHeightSpan
import android.text.style.MetricAffectingSpan
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting

class HeaderSpan constructor(
    @IntRange(from = 1, to = 6)
    private val level: Int,
    @ColorInt
    private val textColor: Int,
    @ColorInt
    private val dividerColor: Int,
    @Px
    private val marginTop: Float,
    @Px
    private val marginBottom: Float
) : MetricAffectingSpan(), LineHeightSpan, LeadingMarginSpan {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val linePadding = 0.4f
    private var originAscent = 0
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val sizes = mapOf(
        1 to 2f,
        2 to 1.5f,
        3 to 1.25f,
        4 to 1f,
        5 to 0.875f,
        6 to 0.85f
    )

    override fun chooseHeight(
        text: CharSequence?,
        start: Int,
        end: Int,
        spanstartv: Int,
        lineHeight: Int,
        fm: Paint.FontMetricsInt?
    ) {

        fm ?: return
        text as Spanned

        val spanStart = text.getSpanStart(this)
        val spanEnd = text.getSpanEnd(this)

        if (spanStart == start) {// Это первая строка
            originAscent = fm.ascent
            fm.ascent = (fm.ascent - marginTop).toInt()
        } else { // не первая линия
            fm.ascent = originAscent // отступ делаем только от первой линии, для остальных восстанавливаем
        }

        if (spanEnd == end.dec()) {// Это последняя линия (dec - чтобы не учитывать разделитель
            val originHeight = fm.descent - originAscent
            fm.descent = (originHeight * linePadding + marginBottom).toInt()
        }
        fm.top = fm.ascent
        fm.bottom = fm.descent
    }

    override fun updateMeasureState(paint: TextPaint) {
        with(paint) {
            textSize *= sizes.getOrElse(level) { 1f } // переопределим размер шрифта
            isFakeBoldText = true
        }
    }

    override fun updateDrawState(tp: TextPaint) { // дергается для отрисовки шрифта
        with(tp) {
            textSize *= sizes.getOrElse(level) { 1f } // переопределим размер шрифта
            isFakeBoldText = true
            color = textColor
        }
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, currentMarginLocation: Int, paragraphDirection: Int,
        lineTop: Int, lineBaseline: Int, lineBottom: Int, text: CharSequence?, lineStart: Int,
        lineEnd: Int, isFirstLine: Boolean, layout: Layout?
    ) {
        if ((level == 1 || level == 2) && (text as Spanned).getSpanEnd(this) == lineEnd) { // Для заголовков 1-2 уровня и последней строки - рисуем отделяющую линию
            paint.forLine {
                val lh = (paint.descent() - paint.ascent()) * sizes.getOrElse(level) { 1f }
                val lineOffset = lineBaseline + lh * linePadding
                canvas.drawLine(
                    0f,
                    lineOffset,
                    canvas.width.toFloat(),
                    lineOffset,
                    paint
                )
            }
        }
        //canvas.drawFontLines(lineTop, lineBottom, lineBaseline, paint)
    }

    override fun getLeadingMargin(first: Boolean): Int {
        return 0
    }

    private inline fun Paint.forLine(block: () -> Unit) {
        val oldColor = color
        val oldStyle = style
        val oldWidth = strokeWidth

        color = dividerColor
        style = Paint.Style.STROKE // просто линия
        strokeWidth = 0f

        block()
        // Восстановим старый цвет - чтобы bullet цветом не продолжил рисовать прочие элементы
        color = oldColor
        style = oldStyle
        strokeWidth = oldWidth
    }

    // Визуальный контроль отступов для текста
    private fun Canvas.drawFontLines( top: Int, bottom: Int, lineBaseline: Int, paint: Paint) {
        drawLine(0f, top+0f, width+0f, top+0f, Paint().apply { color = Color.BLUE })
        drawLine(0f, bottom+0f, width+0f, bottom+0f, Paint().apply { color = Color.GREEN })
        drawLine(0f, lineBaseline+0f, width+0f, lineBaseline+0f, Paint().apply { color = Color.RED })
    }
}