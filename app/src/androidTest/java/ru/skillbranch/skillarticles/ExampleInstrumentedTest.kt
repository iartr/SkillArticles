package ru.skillbranch.skillarticles

import android.text.Spannable
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.text.getSpans
import androidx.core.view.marginBottom
import androidx.core.widget.NestedScrollView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.extensions.indexesOf
import ru.skillbranch.skillarticles.extensions.setMarginOptionally
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.custom.SearchFocusSpan
import ru.skillbranch.skillarticles.ui.custom.SearchSpan
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import java.lang.Thread.sleep

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    companion object {
        @BeforeClass
        @JvmStatic
        fun setup() {
            LocalDataHolder.disableDelay(true)
            NetworkDataHolder.disableDelay(true)
        }
    }

    @Test
    fun module1() {
        val content =
            """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas nibh sapien, consectetur et ultrices quis, convallis sit amet augue. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vestibulum et convallis augue, eu hendrerit diam. Curabitur ut dolor at justo suscipit commodo. Curabitur consectetur, massa sed sodales sollicitudin, orci augue maximus lacus, ut elementum risus lorem nec tellus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Praesent accumsan tempor lorem, quis pulvinar justo. Vivamus euismod risus ac arcu pharetra fringilla.
                Maecenas cursus vehicula erat, in eleifend diam blandit vitae. In hac habitasse platea dictumst. Duis egestas augue lectus, et vulputate diam iaculis id. Aenean vestibulum nibh vitae mi luctus tincidunt. Fusce iaculis molestie eros, ac efficitur odio cursus ac. In at orci eget eros dapibus pretium congue sed odio. Maecenas facilisis, dolor eget mollis gravida, nisi justo mattis odio, ac congue arcu risus sed turpis.
                Sed tempor a nibh at maximus."""

        var actualIndexes = content.indexesOf("sed")
        Assert.assertEquals(listOf(322, 930, 1032, 1060), actualIndexes)

        actualIndexes = content.indexesOf("sed", false)
        Assert.assertEquals(listOf(322, 930, 1032), actualIndexes)

        actualIndexes = content.indexesOf("")
        Assert.assertEquals(listOf<Int>(), actualIndexes)

        actualIndexes = null.indexesOf("")
        Assert.assertEquals(listOf<Int>(), actualIndexes)
    }

    @Test
    fun module2() {
        val scenario = ActivityScenario.launch(RootActivity::class.java)
        var actualBefore = 0
        var actualAfter = 0
        scenario.onActivity { activity ->
            val scroll = activity.findViewById<NestedScrollView>(R.id.scroll)
            actualBefore = scroll.marginBottom
            scroll.setMarginOptionally(bottom = 112)
            actualAfter = scroll.marginBottom
        }
        Assert.assertEquals(0, actualBefore)
        Assert.assertEquals(112, actualAfter)
        scenario.close()
    }

    @Test
    fun module3() {
        val scenario = ActivityScenario.launch(RootActivity::class.java)
        var actualBeforeBg = 0
        var actualBeforeFg = 0

        var actualAfterBg = 0
        var actualAfterFg = 0
        scenario.onActivity { activity ->
            actualBeforeBg = activity.bgColor
            actualBeforeFg = activity.fgColor
            activity.delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        }
        scenario.onActivity { activity ->
            actualAfterBg = activity.bgColor
            actualAfterFg = activity.fgColor
        }
        Assert.assertEquals("#FC4C4C", actualBeforeBg.toHex())
        Assert.assertEquals("#FFFFFF", actualBeforeFg.toHex())
        Assert.assertEquals("#BB86FC", actualAfterBg.toHex())
        Assert.assertEquals("#FFFFFF", actualAfterFg.toHex())
        scenario.close()
    }

    @Test
    fun module4() {
        val scenario = ActivityScenario.launch(RootActivity::class.java)
        var expectedData = ArticleState(
            isShowMenu = true,
            isBigText = true,
            isLoadingContent = false,
            content = listOf("test content"),
            isLike = true,
            isBookmark = true,
            title = "test title",
            category = "test category"
        )

        scenario.onActivity { activity ->
            activity.binding.bind(expectedData)
        }

        Espresso.onView(withId(R.id.submenu))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.btn_like))
            .check(ViewAssertions.matches(isChecked()))
        Espresso.onView(withId(R.id.btn_bookmark))
            .check(ViewAssertions.matches(isChecked()))
        Espresso.onView(withId(R.id.btn_text_up))
            .check(ViewAssertions.matches(isChecked()))
        Espresso.onView(withId(R.id.switch_mode))
            .check(ViewAssertions.matches(not(isChecked())))
        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withFontSize(18f)))
        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withText("test content")))
        Espresso.onView(
            allOf(
                instanceOf(TextView::class.java),
                withParent(withId(R.id.toolbar)),
                withParentIndex(0)
            )
        )
            .check(ViewAssertions.matches(withText("test title")))
        Espresso.onView(
            allOf(
                instanceOf(TextView::class.java),
                withParent(withId(R.id.toolbar)),
                withParentIndex(1)
            )
        )
            .check(ViewAssertions.matches(withText("test category")))

        expectedData = expectedData.copy(isSearch = true, isShowMenu = false, isBigText = false, searchResults = listOf(0 to 2, 4 to 6, 8 to 10, 10 to 12), searchPosition = 3)

        scenario.onActivity { activity ->
            activity.binding.bind(expectedData)
        }

        sleep(500)

        Espresso.onView(withId(R.id.submenu))
            .check(ViewAssertions.matches(not(isDisplayed())))
        Espresso.onView(withId(R.id.group_bottom))
            .check(ViewAssertions.matches(not(isDisplayed())))
        Espresso.onView(withId(R.id.reveal))
            .check(ViewAssertions.matches(isDisplayed()))
        Espresso.onView(withId(R.id.tv_search_result))
            .check(ViewAssertions.matches(withText("4 of 4")))
        Espresso.onView(withId(R.id.btn_result_up))
            .check(ViewAssertions.matches(isEnabled()))
        Espresso.onView(withId(R.id.btn_result_down))
            .check(ViewAssertions.matches(not(isEnabled())))

        expectedData = expectedData.copy(isSearch = true,  searchResults = listOf())

        scenario.onActivity { activity ->
            activity.binding.bind(expectedData)
        }

        Espresso.onView(withId(R.id.tv_search_result))
            .check(ViewAssertions.matches(withText("Not found")))
        Espresso.onView(withId(R.id.btn_result_up))
            .check(ViewAssertions.matches(not(isEnabled())))
        Espresso.onView(withId(R.id.btn_result_down))
            .check(ViewAssertions.matches(not(isEnabled())))

        scenario.close()
    }

    @Test
    fun module5() {
        val scenario = ActivityScenario.launch(RootActivity::class.java)
        val content =
            """Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas nibh sapien, consectetur et ultrices quis, convallis sit amet augue. Interdum et malesuada fames ac ante ipsum primis in faucibus. Vestibulum et convallis augue, eu hendrerit diam. Curabitur ut dolor at justo suscipit commodo. Curabitur consectetur, massa sed sodales sollicitudin, orci augue maximus lacus, ut elementum risus lorem nec tellus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Praesent accumsan tempor lorem, quis pulvinar justo. Vivamus euismod risus ac arcu pharetra fringilla.
                Maecenas cursus vehicula erat, in eleifend diam blandit vitae. In hac habitasse platea dictumst. Duis egestas augue lectus, et vulputate diam iaculis id. Aenean vestibulum nibh vitae mi luctus tincidunt. Fusce iaculis molestie eros, ac efficitur odio cursus ac. In at orci eget eros dapibus pretium congue sed odio. Maecenas facilisis, dolor eget mollis gravida, nisi justo mattis odio, ac congue arcu risus sed turpis.
                Sed tempor a nibh at maximus."""
        val searchResult = listOf(322 to 325, 930 to 933, 1032 to 1035, 1060 to 1063)

        scenario.onActivity { activity ->
            activity.binding.bind(ArticleState().copy(content = listOf(content)))
            activity.showSearchBar()
        }
        sleep(500)

        Espresso.onView(withId(R.id.reveal))
            .check(ViewAssertions.matches(isDisplayed()))

        scenario.onActivity { activity ->
            activity.renderSearchResult(searchResult)
        }

        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withText(content)))
        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withSearchResult(searchResult)))

        scenario.onActivity { activity ->
            activity.renderSearchPosition(3)
        }

        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withSearchPosition(3)))

        scenario.onActivity { activity ->
            activity.clearSearchResult()
        }

        Espresso.onView(withId(R.id.tv_text_content))
            .check(ViewAssertions.matches(withSearchResult(listOf())))


        scenario.onActivity { activity ->
            activity.hideSearchBar()
        }
        sleep(500)
        Espresso.onView(withId(R.id.reveal))
            .check(ViewAssertions.matches(not(isDisplayed())))

        scenario.close()
    }

    private fun Int.toHex(): String = String.format("#%06X", 0xFFFFFF and this)

    private fun withFontSize(expectedSize: Float): Matcher<View> {
        return object : BoundedMatcher<View, View>(TextView::class.java) {

            var actSize: Float? = null

            public override fun matchesSafely(target: View): Boolean {
                if (target !is TextView) return false
                val pixels = target.textSize
                val actualSize = pixels / target.getResources().displayMetrics.scaledDensity
                actSize = actualSize
                return actualSize == expectedSize
            }

            override fun describeTo(description: Description) {
                if (actSize == null) {
                    description.appendText("with fontSize, expected fontSize : ")
                    description.appendValue(expectedSize)
                } else {
                    description.appendText("with fontSize, actual fontSize : ")
                    description.appendValue(actSize)
                    description.appendText(" expected fontSize : ")
                    description.appendValue(expectedSize)
                }

            }
        }
    }

    private fun withSearchResult(searchResult: List<Pair<Int, Int>>): Matcher<View> {
        return object : BoundedMatcher<View, View>(TextView::class.java) {
            var actPositions: List<Pair<Int, Int>>? = null

            public override fun matchesSafely(target: View): Boolean {
                if (target !is TextView) return false
                if (target.text !is Spannable) return false
                val content = target.text as Spannable
                val searchSpans = content.getSpans<SearchSpan>()
                    .filter { it !is SearchFocusSpan }
                val spansPositions =
                    searchSpans.map { content.getSpanStart(it) to content.getSpanEnd(it) }
                actPositions = spansPositions
                return searchResult == spansPositions
            }

            override fun describeTo(description: Description) {
                if (actPositions == null) {
                    description.appendText("with search result, expected spans positions:  ")
                    description.appendValue(searchResult)
                } else {
                    description.appendText("with search result, actual spans positions:  ")
                    description.appendValue(actPositions)
                    description.appendText(" expected spans positions : ")
                    description.appendValue(searchResult)
                }
            }
        }
    }

    private fun withSearchPosition(position: Int): Matcher<View> {
        return object : BoundedMatcher<View, View>(TextView::class.java) {
            var actPosition: Int? = null

            public override fun matchesSafely(target: View): Boolean {
                if (target !is TextView) return false
                if (target.text !is Spannable) return false

                val content = target.text as Spannable
                val searchSpans = content.getSpans<SearchSpan>()
                    .filter { it !is SearchFocusSpan }
                    .map {content.getSpanStart(it) to content.getSpanEnd(it)}

                val focusSpan = content.getSpans<SearchFocusSpan>()
                    .map {content.getSpanStart(it) to content.getSpanEnd(it)}
                    .firstOrNull()

                actPosition = searchSpans.indexOfFirst { it == focusSpan }
                return actPosition == position
            }

            override fun describeTo(description: Description) {
                if (actPosition == null) {
                    description.appendText("with search position, expected search focus span position: ")
                    description.appendValue(position)
                } else {
                    description.appendText("with search position, actual search focus span position : ")
                    description.appendValue(actPosition)
                    description.appendText(" expected search focus span position : ")
                    description.appendValue(position)
                }
            }
        }
    }

}


