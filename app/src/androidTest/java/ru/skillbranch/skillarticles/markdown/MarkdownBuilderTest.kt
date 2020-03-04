package ru.skillbranch.skillarticles.markdown

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import ru.skillbranch.skillarticles.markdown.spans.*

@RunWith(AndroidJUnit4::class)
class MarkdownBuilderTest {

    @Test
    fun draw_list_item() {
        //settings
        val color = Color.RED
        val gap = 24f
        val radius = 8f

        //defaults
        val defaultColor = Color.GRAY
        val cml = 0 // Текущий отступ от начала строки
        val ltop = 0 // line top
        val lbase = 60 // line baseline
        val lbottom = 80 // line bottom

        //mocks
        val canvas = Mockito.mock(Canvas::class.java)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        val layout = Mockito.mock(Layout::class.java)

        val text = SpannableString("text")

        val span = UnorderedListSpan(gap, radius, color)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Проверка отступа абзаца. Равен двум деаметрам + заданный отступ от circle
        Assert.assertEquals((4 * radius + gap).toInt(), span.getLeadingMargin(true))

        //check bullet draw
        span.drawLeadingMargin(
            canvas, paint, cml, 1,
            ltop, lbase, lbottom, text, 0, text.length,
            true, layout
        )

        // check order call
        val inOrder = Mockito.inOrder(paint, canvas)
        //check first set color to paint
        inOrder.verify(paint).color = color
        //check draw circle bullet
        inOrder.verify(canvas).drawCircle(
            gap + cml + radius,
            (lbottom - ltop) / 2f + ltop,
            radius,
            paint
        )
        // check paint color restore
        inOrder.verify(paint).color = defaultColor
    }

    @Test
    fun draw_quote() {
        //settings
        val color = Color.RED
        val gap = 24f
        val lineWidth = 8f

        //defaults
        val defaultColor = Color.GRAY
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom

        //mocks
        val canvas = Mockito.mock(Canvas::class.java)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        val layout = Mockito.mock(Layout::class.java)

        val text = SpannableString("text")

        val span = BlockquotesSpan(gap, lineWidth, color)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check leading margin
        Assert.assertEquals((lineWidth + gap).toInt(), span.getLeadingMargin(true))

        //check line draw
        span.drawLeadingMargin(
            canvas, paint, cml, 1,
            ltop, lbase, lbottom, text, 0, text.length,
            true, layout
        )

        //check order call
        val inOrder = Mockito.inOrder(paint, canvas)
        //check first set color to paint
        inOrder.verify(paint).color = color
        inOrder.verify(paint).strokeWidth = lineWidth
        //check draw circle bullet
        inOrder.verify(canvas).drawLine(
            lineWidth / 2f,
            ltop.toFloat(),
            lineWidth / 2,
            lbottom.toFloat(),
            paint
        )
        //check paint color restore
        inOrder.verify(paint).color = defaultColor
    }

    @Test
    fun draw_header() {
        //settings
        val levels = 1..6
        val textColor = Color.RED
        val lineColor = Color.GREEN
        val marginTop = 24f
        val marginBottom = 16f

        //defaults
        val canvasWidth = 700
        val defaultColor = Color.GRAY
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom
        val defaultAscent = -30
        val defaultDescent = 10

        for (level in levels){
            //mocks
            val canvas = Mockito.mock(Canvas::class.java)
            Mockito.`when`(canvas.width).thenReturn(canvasWidth)
            val paint = Mockito.mock(Paint::class.java)
            Mockito.`when`(paint.color).thenReturn(defaultColor)
            val measurePaint = Mockito.mock(TextPaint::class.java)
            val drawPaint = Mockito.mock(TextPaint::class.java)
            val layout = Mockito.mock(Layout::class.java)
            val fm = Mockito.mock(Paint.FontMetricsInt::class.java)
            fm.ascent = defaultAscent
            fm.descent = defaultDescent

            val text = SpannableString("text")

            val span = HeaderSpan(level, textColor, lineColor, marginTop, marginBottom)
            text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            //check leading margin
            Assert.assertEquals(0, span.getLeadingMargin(true))

            //check measure state
            span.updateMeasureState(measurePaint)
            Mockito.verify(measurePaint).textSize *= span.sizes[level]!!
            Mockito.verify(measurePaint).isFakeBoldText = true

            //check draw state
            span.updateDrawState(drawPaint)
            Mockito.verify(drawPaint).textSize *= span.sizes[level]!!
            Mockito.verify(drawPaint).isFakeBoldText = true
            Mockito.verify(drawPaint).color = textColor

            //check change line height
            span.chooseHeight(text,0, text.length.inc(), 0,0, fm)
            Assert.assertEquals((defaultAscent - marginTop).toInt(), fm.ascent)
            Assert.assertEquals(
                ((defaultDescent - defaultAscent) * span.linePadding + marginBottom).toInt(),
                fm.descent
            )
            Assert.assertEquals(fm.top, fm.ascent)
            Assert.assertEquals(fm.bottom, fm.descent)

            //check line draw
            span.drawLeadingMargin(
                canvas, paint, cml, 1,
                ltop, lbase, lbottom, text, 0, text.length,
                true, layout
            )

            val inOrder = Mockito.inOrder(paint, canvas)

            if (level == 1 || level == 2){
                inOrder.verify(paint).color = lineColor
                val lh = (paint.descent() - paint.ascent()) * span.sizes[level]!!
                val lineOffset = lbase + lh * span.linePadding
                inOrder.verify(canvas).drawLine(0f, lineOffset, canvasWidth.toFloat(), lineOffset, paint)

                inOrder.verify(paint).color = defaultColor
            }
        }
    }

    @Test
    fun draw_rule() {
        //settings
        val color = Color.RED
        val width = 24f

        //defaults
        val canvasWidth = 700
        val defaultColor = Color.GRAY
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom

        //mocks
        val canvas = Mockito.mock(Canvas::class.java)
        Mockito.`when`(canvas.width).thenReturn(canvasWidth)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)

        val text = SpannableString("text")

        val span = HorizontalRuleSpan(width, color)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check draw rule line
        span.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrder = Mockito.inOrder(paint, canvas)

        inOrder.verify(paint).color = color

        inOrder.verify(canvas).drawLine(
            0f,
            (ltop + lbottom) / 2f,
            canvasWidth.toFloat(),
            (ltop + lbottom) / 2f,
            paint
        )

        inOrder.verify(paint).color = defaultColor


    }

    @Test
    fun draw_inline_code() {
        //settings
        val textColor: Int = Color.RED
        val bgColor: Int = Color.GREEN
        val cornerRadius: Float = 8f
        val padding: Float = 8f

        //defaults
        val canvasWidth = 700
        val defaultColor = Color.GRAY
        val measureText = 100f
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom

        //mocks
        val canvas = Mockito.mock(Canvas::class.java)
        Mockito.`when`(canvas.width).thenReturn(canvasWidth)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        Mockito.`when`(
            paint.measureText(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(measureText)
        val fm = Mockito.mock(Paint.FontMetricsInt::class.java)

        val text = SpannableString("text")

        val span = InlineCodeSpan(textColor, bgColor, cornerRadius, padding)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check measure size
        val size = span.getSize(paint, text, 0, text.length, fm)
        Assert.assertEquals((2 * padding + measureText).toInt(), size)


        //check draw inline code
        span.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrder = Mockito.inOrder(paint, canvas)

        //check draw background
        inOrder.verify(paint).color = bgColor
        inOrder.verify(canvas).drawRoundRect(
            RectF(0f, ltop.toFloat(), measureText + 2 * padding, lbottom.toFloat()),
            cornerRadius,
            cornerRadius,
            paint
        )

        //check draw text
        inOrder.verify(paint).color = textColor
        inOrder.verify(canvas).drawText(text, 0, text.length, cml + padding, lbase.toFloat(), paint)
        inOrder.verify(paint).color = defaultColor
    }

    @Test
    fun draw_link() {
        //settings
        val iconColor: Int = Color.RED
        val padding: Float = 8f
        val textColor: Int = Color.BLUE

        //defaults
        val canvasWidth = 700
        val defaultColor = Color.GRAY
        val measureText = 100f
        val defaultAscent = -30
        val defaultDescent = 10
        val cml = 0 //current margin location
        val ltop = 0 //line top
        val lbase = 60 //line baseline
        val lbottom = 80 //line bottom

        //mocks
        val canvas = Mockito.mock(Canvas::class.java)
        Mockito.`when`(canvas.width).thenReturn(canvasWidth)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        Mockito.`when`(
            paint.measureText(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(measureText)
        val fm = Mockito.mock(Paint.FontMetricsInt::class.java)
        fm.ascent = defaultAscent
        fm.descent = defaultDescent

        //spy
        val linkDrawable: Drawable = Mockito.spy(VectorDrawable())
        val path: Path = Mockito.spy(Path())

        val text = SpannableString("text")

        val span = IconLinkSpan(linkDrawable, iconColor, padding, textColor)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.path = path

        //check measure size
        val size = span.getSize(paint, text, 0, text.length, fm)
        Assert.assertEquals((defaultDescent - defaultAscent + padding + measureText).toInt(), size)

        //check drawable set bounds and set tint
        Mockito.verify(linkDrawable).setBounds(0, 0, fm.descent - fm.ascent, fm.descent - fm.ascent)
        Mockito.verify(linkDrawable).setTint(iconColor)

        //check draw icon and text
        span.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrder = Mockito.inOrder(paint, canvas, path, linkDrawable)

        //check path effect
        Mockito.verify(paint, Mockito.atLeastOnce()).pathEffect = Mockito.any()
        Mockito.verify(paint, Mockito.atLeastOnce()).strokeWidth = 0f
        inOrder.verify(paint).color = textColor

        //check reset path
        inOrder.verify(path).reset() //check reset before draw
        Mockito.verify(path).moveTo(cml + span.iconSize + padding, lbottom.toFloat())
        Mockito.verify(path).lineTo(cml + span.iconSize + padding + span.textWidth, lbottom.toFloat())

        //check draw path
        inOrder.verify(canvas).drawPath(path, paint)

        //check draw icon
        inOrder.verify(canvas).save()
        inOrder.verify(canvas).translate(
            cml.toFloat(),
            (lbottom - linkDrawable.bounds.bottom).toFloat()
        )
        inOrder.verify(linkDrawable).draw(canvas)
        inOrder.verify(canvas).restore()

        //check draw text
        inOrder.verify(paint).color = textColor
        inOrder.verify(canvas).drawText(
            text,
            0,
            text.length,
            cml + span.iconSize + padding,
            lbase.toFloat(),
            paint
        )
        inOrder.verify(paint).color = defaultColor
    }

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
        val canvas = Mockito.mock(Canvas::class.java)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        val layout = Mockito.mock(Layout::class.java)

        val text = SpannableString("text")

        val span = OrderedListSpan(gap, order, color)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check leading margin
        Assert.assertEquals((order.length.inc() * gap).toInt(), span.getLeadingMargin(true))

        //check bullet draw
        span.drawLeadingMargin(
            canvas, paint, cml, 1,
            ltop, lbase, lbottom, text, 0, text.length,
            true, layout
        )

        //check order call
        val inOrder = Mockito.inOrder(paint, canvas)
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
        val canvas = Mockito.mock(Canvas::class.java)
        Mockito.`when`(canvas.width).thenReturn(canvasWidth)
        val paint = Mockito.mock(Paint::class.java)
        Mockito.`when`(paint.color).thenReturn(defaultColor)
        Mockito.`when`(paint.ascent()).thenReturn(defaultAscent * 0.85f)
        Mockito.`when`(paint.descent()).thenReturn(defaultDescent * 0.85f)
        val fm = Mockito.mock(Paint.FontMetricsInt::class.java)
        val path = Mockito.spy(Path())

        val text = SpannableString("text")

        //check SINGLE type
        val span =
            BlockCodeSpan(textColor, bgColor, cornerRadius, padding, Element.BlockCode.Type.SINGLE)
        text.setSpan(span, 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        //check lineHeight SINGLE type
        fm.ascent = defaultAscent
        fm.descent = defaultDescent
        val size = span.getSize(paint, text, 0, text.length, fm)
        Assert.assertEquals(0, size)
        Assert.assertEquals((defaultAscent * 0.85f - 2 * padding).toInt(), fm.ascent)
        Assert.assertEquals((defaultDescent * 0.85f + 2 * padding).toInt(), fm.descent)

        span.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrder = Mockito.inOrder(paint, canvas)

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
        Assert.assertEquals((defaultAscent * 0.85f - 2 * padding).toInt(), fm.ascent)
        Assert.assertEquals((defaultDescent * 0.85f).toInt(), fm.descent)

        spanStart.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderStart = Mockito.inOrder(paint, canvas, path)

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
        Assert.assertEquals((defaultAscent * 0.85f).toInt(), fm.ascent)
        Assert.assertEquals((defaultDescent * 0.85f).toInt(), fm.descent)

        spanMiddle.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderMiddle = Mockito.inOrder(paint, canvas)

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
        Assert.assertEquals((defaultAscent * 0.85f).toInt(), fm.ascent)
        Assert.assertEquals((defaultDescent * 0.85f + 2 * padding).toInt(), fm.descent)

        spanEnd.draw(canvas, text, 0, text.length, cml.toFloat(), ltop, lbase, lbottom, paint)

        val inOrderEnd = Mockito.inOrder(paint, canvas, path)

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