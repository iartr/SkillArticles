package ru.skillbranch.skillarticles

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.widget.TextView
import androidx.core.text.buildSpannedString
import androidx.core.view.setPadding
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.*
import ru.skillbranch.skillarticles.data.longText
import ru.skillbranch.skillarticles.data.repositories.MarkdownParser
import ru.skillbranch.skillarticles.data.repositories.clearContent
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.extensions.groupByBounds
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownCodeView
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownImageView
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownTextView
import ru.skillbranch.skillarticles.ui.custom.markdown.SearchBgHelper
import ru.skillbranch.skillarticles.ui.custom.spans.HeaderSpan
import ru.skillbranch.skillarticles.utils.TestActivity
import java.lang.Thread.sleep

class InstrumentalTest1 {

    @Test
    fun group_by_bounds() {
        val query = "background"
        val expectedResult: List<List<Pair<Int, Int>>> = listOf(
            listOf(25 to 35, 92 to 102, 153 to 163),
            listOf(220 to 230),
            listOf(239 to 249),
            listOf(330 to 340),
            listOf(349 to 359),
            listOf(421 to 431),
            listOf(860 to 870, 954 to 964),
            listOf(1084 to 1094),
            listOf(1209 to 1219, 1355 to 1365, 1795 to 1805),
            listOf(),
            listOf(
                2115 to 2125,
                2357 to 2367,
                2661 to 2671,
                2807 to 2817,
                3314 to 3324,
                3348 to 3358,
                3423 to 3433,
                3623 to 3633,
                3711 to 3721,
                4076 to 4086
            ),
            listOf(),
            listOf(5766 to 5776, 5897 to 5907, 5939 to 5949),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf(),
            listOf()
        )
        val rawContent = MarkdownParser.parse(longText)

        val bounds = rawContent.map { it.bounds }

        val searchResult = rawContent.clearContent()
            .indexesOf(query)
            .map { it to it + query.length }

        val result = searchResult.groupByBounds(bounds)

        assertEquals(expectedResult, result)
    }

    @Test
    fun draw_search_background() {

        val string = buildSpannedString {
            append(
                "Header1 for first line and for second line also header for third line",
                HeaderSpan(1, Color.BLACK, Color.GRAY, 24f, 16f),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            append("\nsimple text on line")
        }

        val mockDrawable = mock(Drawable::class.java)

        //single line
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        scenario.onActivity {
            val helper = spy(SearchBgHelper(it, mockDrawable = mockDrawable))
            val mv = MarkdownTextView(it, 14f, helper).apply {
                setText(string, TextView.BufferType.SPANNABLE)
                setPadding(it.dpToIntPx(16))
                renderSearchResult(listOf(0 to 11), 0)
                id = 100
            }
            it.setContentView(mv)
        }
        sleep(1000)

        val inOrder = inOrder(mockDrawable)
        val singleRes = argumentCaptor<Int>()
        inOrder.verify(mockDrawable).setBounds(
            singleRes.capture(),
            singleRes.capture(),
            singleRes.capture(),
            singleRes.capture()
        )
        assertEquals(
            "single line drawable setBounds ",
            "left: -8, top: 24, right: 304, bottom : 90",
            "left: ${singleRes.allValues[0]}, top: ${singleRes.allValues[1]}, right: ${singleRes.allValues[2]}, bottom : ${singleRes.allValues[3]}"
        )
        inOrder.verify(mockDrawable).draw(any())

        //multi line
        scenario.onActivity {
            val mv = it.findViewById<MarkdownTextView>(100)
            mv.renderSearchResult(listOf(27 to 65), 0)
        }
        sleep(1000)

        val multRes = argumentCaptor<Int>()
        inOrder.verify(mockDrawable)
            .setBounds(multRes.capture(), multRes.capture(), multRes.capture(), multRes.capture())
        assertEquals(
            "first line drawable setBounds",
            "left: 623, top: 24, right: 709, bottom : 90",
            "left: ${multRes.allValues[0]}, top: ${multRes.allValues[1]}, right: ${multRes.allValues[2]}, bottom : ${multRes.allValues[3]}"
        )
        inOrder.verify(mockDrawable).draw(any())
        inOrder.verify(mockDrawable)
            .setBounds(multRes.capture(), multRes.capture(), multRes.capture(), multRes.capture())
        assertEquals(
            "middle line drawable setBounds",
            "left: -8, top: 90, right: 683, bottom : 156",
            "left: ${multRes.allValues[4]}, top: ${multRes.allValues[5]}, right: ${multRes.allValues[6]}, bottom : ${multRes.allValues[7]}"
        )
        inOrder.verify(mockDrawable).draw(any())
        inOrder.verify(mockDrawable)
            .setBounds(multRes.capture(), multRes.capture(), multRes.capture(), multRes.capture())
        assertEquals(
            "last line drawable setBounds",
            "left: -8, top: 156, right: 135, bottom : 222",
            "left: ${multRes.allValues[8]}, top: ${multRes.allValues[9]}, right: ${multRes.allValues[10]}, bottom : ${multRes.allValues[11]}"
        )
        inOrder.verify(mockDrawable).draw(any())

        //simple text
        scenario.onActivity {
            val mv = it.findViewById<MarkdownTextView>(100)
            mv.renderSearchResult(listOf(70 to 76), 0)
        }
        sleep(1000)
        val simpleRes = argumentCaptor<Int>()
        inOrder.verify(mockDrawable).setBounds(
            simpleRes.capture(),
            simpleRes.capture(),
            simpleRes.capture(),
            simpleRes.capture()
        )
        assertEquals(
            "simple text drawable setBounds ",
            "left: -8, top: 250, right: 92, bottom : 283",
            "left: ${simpleRes.allValues[0]}, top: ${simpleRes.allValues[1]}, right: ${simpleRes.allValues[2]}, bottom : ${simpleRes.allValues[3]}"
        )
        inOrder.verify(mockDrawable).draw(any())
    }

    @Test
    fun draw_markdown_image_view() {

        var viewUnderTest: MarkdownImageView? = null
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        scenario.onActivity {
            viewUnderTest = spy(MarkdownImageView(it, 14f, "any", "title", "alt title"))
            viewUnderTest!!.iv_image.setImageDrawable(it.getDrawable(R.drawable.logo))
            it.setContentView(viewUnderTest)
        }
        sleep(1000)

        val inOrder = inOrder(viewUnderTest)
        inOrder.verify(viewUnderTest)!!.onMeasure(anyInt(), anyInt())
        inOrder.verify(viewUnderTest)!!.onLayout(anyBoolean(), eq(0), eq(0), eq(768), eq(725))
        inOrder.verify(viewUnderTest)!!.dispatchDraw(any())

        assertEquals("markdown view measure width", 768, viewUnderTest!!.measuredWidth)
        assertEquals("markdown view measure height", 725, viewUnderTest!!.measuredHeight)
        assertEquals(
            "markdown iv_image measure height",
            680,
            viewUnderTest!!.iv_image.measuredHeight
        )
        assertEquals(
            "markdown tv_title measure height",
            29,
            viewUnderTest!!.tv_title.measuredHeight
        )
        assertEquals(
            "markdown tv_alt measure height",
            70,
            viewUnderTest!!.tv_alt!!.measuredHeight
        )

        val mockCanvas = mock(Canvas::class.java)
        `when`(mockCanvas.width).thenReturn(768)
        viewUnderTest!!.dispatchDraw(mockCanvas)
        verify(mockCanvas).drawLine(eq(0f), eq(710.5f), eq(112f), eq(710.5f), any())
        verify(mockCanvas).drawLine(eq(656f), eq(710.5f), eq(768f), eq(710.5f), any())

    }

    @Test
    fun draw_markdown_scroll_view() {
        var viewUnderTest: MarkdownCodeView? = null
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        scenario.onActivity {
            viewUnderTest = spy(MarkdownCodeView(it, 14f, "any code"))
            viewUnderTest!!.id = 100
            it.setContentView(viewUnderTest)
        }
        sleep(1000)
        verify(viewUnderTest)!!.onMeasure(anyInt(), anyInt())
        verify(viewUnderTest, atLeastOnce())!!.onLayout(anyBoolean(), eq(0), eq(0), eq(768), eq(65))

        assertEquals("markdown view measure width", 768, viewUnderTest!!.measuredWidth)
        assertEquals("markdown view measure height", 65, viewUnderTest!!.measuredHeight)
        assertEquals("markdown scroll measure height", 33, viewUnderTest!!.sv_scroll.measuredHeight)
        assertEquals(
            "markdown switch measure top & right",
            "top: 20 right: 716",
            "top: ${viewUnderTest!!.iv_switch.top} right: ${viewUnderTest!!.iv_switch.right}"
        )
        assertEquals(
            "markdown copy measure top & right",
            "top: 20 right: 752",
            "top: ${viewUnderTest!!.iv_copy.top} right: ${viewUnderTest!!.iv_copy.right}"
        )

        scenario.onActivity {
            val cv = it.findViewById<MarkdownCodeView>(100)
            cv.removeView(cv)
            viewUnderTest = spy(MarkdownCodeView(it, 14f, "first line\nsecond line"))
            it.setContentView(viewUnderTest)
        }
        sleep(1000)


        verify(viewUnderTest)!!.onMeasure(anyInt(), anyInt())
        verify(viewUnderTest, atLeastOnce())!!.onLayout(anyBoolean(), eq(0), eq(0), eq(768), eq(93))

        assertEquals("markdown view measure width", 768, viewUnderTest!!.measuredWidth)
        assertEquals("markdown view measure height", 93, viewUnderTest!!.measuredHeight)
        assertEquals("markdown scroll measure height", 61, viewUnderTest!!.sv_scroll.measuredHeight)
        assertEquals(
            "markdown switch measure top & right",
            "top: 16 right: 716",
            "top: ${viewUnderTest!!.iv_switch.top} right: ${viewUnderTest!!.iv_switch.right}"
        )
        assertEquals(
            "markdown copy measure top & right",
            "top: 16 right: 752",
            "top: ${viewUnderTest!!.iv_copy.top} right: ${viewUnderTest!!.iv_copy.right}"
        )

    }

}

inline fun <reified T : Any> argumentCaptor(): ArgumentCaptor<T> =
    ArgumentCaptor.forClass(T::class.java)

private fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T