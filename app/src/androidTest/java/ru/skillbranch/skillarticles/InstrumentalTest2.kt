package ru.skillbranch.skillarticles

import android.graphics.*
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import ru.skillbranch.skillarticles.markdown.Element
import ru.skillbranch.skillarticles.markdown.spans.BlockCodeSpan
import ru.skillbranch.skillarticles.markdown.spans.OrderedListSpan

@RunWith(AndroidJUnit4::class)
class InstrumentedTest2 {

    @Test
    fun draw_ordered_list_item() {
        //settings
        val color = Color.RED
        val gap = 24f
        val order = "1"

        //defaults
        val defaultColor = Color.GRAY
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom

        //mocks
        val canvas = mock(Canvas::class.java)
        val paint = mock(Paint::class.java)
        `when`(paint.color).thenReturn(defaultColor)
        val layout = mock(Layout::class.java)

        val text = SpannableString("text")

        val span = OrderedListSpan(gap, order, color)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check leading margin
        assertEquals((order.length.inc() * gap).toInt(), span.getLeadingMargin(true))

        //check bullet draw
        span.drawLeadingMargin(
            canvas, paint, cml, 1,
            ltop, lbase, lbottom, text, 0, text.length,
            true, layout
        )

        //check order call
        val inOrder = inOrder(paint, canvas)
        //check first set color to paint
        inOrder.verify(paint).color = color
        //check draw circle bullet
        inOrder.verify(canvas).drawText(
            order,
            cml + gap,
            lbase.toFloat(),
            paint
        )
        //check paint color restore
        inOrder.verify(paint).color = defaultColor
    }

    @Test
    fun draw_block_code() {
        //settings
        val textColor = Color.RED
        val bgColor = Color.GREEN
        val cornerRadius = 8f
        val padding = 8f

        //defaults
        val defaultColor = Color.GRAY
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom
        val canvasWidth = 700 //line bottom
        val defaultAscent = -30
        val defaultDescent = 10


        //mocks
        val canvas = mock(Canvas::class.java)
        `when`(canvas.width).thenReturn(canvasWidth)
        val paint = mock(Paint::class.java)
        `when`(paint.color).thenReturn(defaultColor)
        `when`(paint.ascent()).thenReturn(defaultAscent * 0.85f)
        `when`(paint.descent()).thenReturn(defaultDescent * 0.85f)
        val fm = mock(Paint.FontMetricsInt::class.java)
        val path = spy(Path())

        val text = SpannableString("text")

        //check SINGLE type
        val span =
            BlockCodeSpan(textColor, bgColor, cornerRadius, padding, Element.BlockCode.Type.SINGLE)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check lineHeight SINGLE type
        fm.ascent = defaultAscent
        fm.descent = defaultDescent
        val size = span.getSize(paint, text, 0, text.length, fm)
        assertEquals(0, size)
        assertEquals((defaultAscent * 0.85f - 2 * padding).toInt(), fm.ascent)
        assertEquals((defaultDescent * 0.85f + 2 * padding).toInt(), fm.descent)

        span.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrder = inOrder(paint, canvas)

        inOrder.verify(paint).color = bgColor
        inOrder.verify(canvas).drawRoundRect(
            RectF(
                0f,
                ltop + padding,
                canvas.width.toFloat(),
                lbottom - padding
            ),
            cornerRadius,
            cornerRadius,
            paint
        )

        //check draw text
        inOrder.verify(paint).color = textColor
        inOrder.verify(canvas).drawText(text, 0, text.length, cml + padding, lbase.toFloat(), paint)
        inOrder.verify(paint).color = defaultColor


        //check START type
        val spanStart =
            BlockCodeSpan(textColor, bgColor, cornerRadius, padding, Element.BlockCode.Type.START)
        spanStart.path = path
        text.setSpan(spanStart, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check lineHeight START type
        spanStart.getSize(paint, text, 0, text.length, fm)
        assertEquals((defaultAscent * 0.85f - 2 * padding).toInt(), fm.ascent)
        assertEquals((defaultDescent * 0.85f).toInt(), fm.descent)

        spanStart.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderStart = inOrder(paint, canvas, path)

        inOrderStart.verify(paint).color = bgColor

        inOrderStart.verify(path).reset()
        inOrderStart.verify(path).addRoundRect(
            RectF(
                0f,
                ltop + padding,
                canvas.width.toFloat(),
                lbottom.toFloat()
            ),
            floatArrayOf(
                cornerRadius, cornerRadius, // Top left radius in px
                cornerRadius, cornerRadius, // Top right radius in px
                0f, 0f, // Bottom right radius in px
                0f, 0f // Bottom left radius in px
            ),
            Path.Direction.CW
        )
        inOrderStart.verify(canvas).drawPath(path, paint)


        //check MIDDLE type
        val spanMiddle =
            BlockCodeSpan(textColor, bgColor, cornerRadius, padding, Element.BlockCode.Type.MIDDLE)
        text.setSpan(spanMiddle, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check lineHeight MIDDLE type
        fm.ascent = defaultAscent
        fm.descent = defaultDescent
        spanMiddle.getSize(paint, text, 0, text.length, fm)
        assertEquals((defaultAscent * 0.85f).toInt(), fm.ascent)
        assertEquals((defaultDescent * 0.85f).toInt(), fm.descent)

        spanMiddle.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderMiddle = inOrder(paint, canvas)

        inOrderMiddle.verify(paint).color = bgColor
        inOrderMiddle.verify(canvas).drawRect(
            RectF(
                0f,
                ltop.toFloat(),
                canvas.width.toFloat(),
                lbottom.toFloat()
            ),
            paint
        )

        //check END type
        val spanEnd =
            BlockCodeSpan(textColor, bgColor, cornerRadius, padding, Element.BlockCode.Type.END)
        text.setSpan(spanEnd, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanEnd.path = path

        //check lineHeight END type
        fm.ascent = defaultAscent
        fm.descent = defaultDescent
        spanEnd.getSize(paint, text, 0, text.length, fm)
        assertEquals((defaultAscent * 0.85f).toInt(), fm.ascent)
        assertEquals((defaultDescent * 0.85f + 2 * padding).toInt(), fm.descent)

        spanEnd.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderEnd = inOrder(paint, canvas, path)

        inOrderEnd.verify(paint).color = bgColor
        inOrderEnd.verify(path).reset()
        inOrderEnd.verify(path).addRoundRect(
            RectF(
                0f,
                ltop.toFloat(),
                canvas.width.toFloat(),
                lbottom - padding
            ),
            floatArrayOf(
                0f, 0f,
                0f, 0f,
                cornerRadius, cornerRadius,
                cornerRadius, cornerRadius
            ),
            Path.Direction.CW
        )
        inOrderEnd.verify(canvas).drawPath(path, paint)
    }
}


