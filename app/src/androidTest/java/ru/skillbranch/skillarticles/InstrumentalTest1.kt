package ru.skillbranch.skillarticles

import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ScrollToAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import junit.framework.Assert.assertEquals
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.item_article.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.core.AnyOf.anyOf
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.articles.ArticleVH
import ru.skillbranch.skillarticles.ui.custom.markdown.MarkdownContentView
import java.lang.Thread.sleep

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class InstrumentalTest1 {
    @get:Rule
    var activityRule: ActivityTestRule<RootActivity> = ActivityTestRule(RootActivity::class.java)

    @Test
    fun module1() {
        onView(withId(R.id.nav_bookmarks))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Bookmarks fragment",
                "Bookmarks",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.nav_transcriptions))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Transcriptions fragment",
                "Transcriptions",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.nav_articles))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Articles fragment",
                "Articles",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Transcriptions fragment",
                "Transcriptions",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Bookmarks fragment",
                "Bookmarks",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Articles fragment",
                "Articles",
                toolbar.title.toString()
            )
        }
    }

    @Test
    fun module2() {
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            assertEquals(
                "RecycleView ArticleItemView count",
                6,
                rv.adapter!!.itemCount
            )

        }
        onView(withId(R.id.rv_articles))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(3, click()));

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Article fragment",
                "Observe LiveData from ViewModel in Fragment",
                toolbar.title.toString()
            )

            assertEquals(
                "subtitle after navigate to Article fragment",
                "Android",
                toolbar.subtitle.toString()
            )
        }
    }

    @Test
    fun module3() {
        onView(withId(R.id.rv_articles))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(2, click()));

        activityRule.activity.run {
            assertEquals(
                "Title in toolbar Article fragment",
                "Using Safe args plugin — current state of affairs",
                toolbar.title.toString()
            )

            assertEquals(
                "Subtitle in toolbar Article fragment",
                "Android",
                toolbar.subtitle.toString()
            )

            assertEquals(
                "Article Title in Article fragment",
                "Using Safe args plugin — current state of affairs",
                tv_title.text.toString()
            )

            assertEquals(
                "Article date in Article fragment",
                "11:03:02 11.04.20",
                tv_date.text.toString()
            )

            assertEquals(
                "Article Author in Article fragment",
                "Veronika Petruskova",
                tv_author.text.toString()
            )
        }
        sleep(2000)

        activityRule.activity.run {
            assertEquals(
                "Markdown Content View child count",
                33,
                findViewById<MarkdownContentView>(R.id.tv_text_content).childCount
            )
        }

        onView(withId(R.id.et_comment))
            .perform(customScrollTo())
            .check(matches(isDisplayed()))

    }

    @Test
    fun module4() {
        onView(withId(R.id.nav_profile))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Profile fragment",
                "Authorization",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.tv_privacy))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Privacy fragment",
                "Privacy policy",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Authorization fragment",
                "Authorization",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Articles fragment",
                "Articles",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.rv_articles))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(1, click()));

        activityRule.activity.run {
            assertEquals(
                "Title in toolbar Article fragment",
                "Architecture Components pitfalls",
                toolbar.title.toString()
            )
        }
        sleep(2000)

        onView(withId(R.id.et_comment))
            .perform(customScrollTo(), replaceText("comment text"), pressImeActionButton())

        activityRule.activity.run {
            assertEquals(
                "title after send comment",
                "Authorization",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.tv_privacy))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Privacy fragment",
                "Privacy policy",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Authorization fragment",
                "Authorization",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.btn_login))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "Title in toolbar Article fragment after login",
                "Architecture Components pitfalls",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Articles fragment",
                "Articles",
                toolbar.title.toString()
            )
        }

        onView(withId(R.id.nav_profile))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "title after navigate to Profile fragment after login",
                "Profile",
                toolbar.title.toString()
            )
        }
    }

    @Test
    fun module5() {
        onView(withId(R.id.nav_bookmarks))
            .perform(click())

        onView(withId(R.id.nav_transcriptions))
            .perform(click())

        onView(withId(R.id.nav_articles))
            .perform(click())

        pressBack()

        onView(withId(R.id.nav_transcriptions))
            .check(matches(withBottomNavItemCheckedStatus(true)))
    }

    @Test
    fun module6() {
        onView(withId(R.id.nav_bookmarks))
            .perform(click())

        onView(withId(R.id.nav_transcriptions))
            .perform(click())

        onView(withId(R.id.nav_profile))
            .perform(click())

        onView(withId(R.id.nav_articles))
            .perform(click())

        onView(withId(R.id.nav_profile))
            .perform(click())

        onView(withId(R.id.btn_login))
            .perform(click())

        activityRule.activity.run {
            assertEquals(
                "Title in toolbar Profile fragment after login",
                "Profile",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Articles fragment",
                "Articles",
                toolbar.title.toString()
            )
        }

        pressBack()

        activityRule.activity.run {
            assertEquals(
                "title after back to Profile fragment",
                "Profile",
                toolbar.title.toString()
            )
        }
    }

    private fun customScrollTo(): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return allOf(
                withEffectiveVisibility(Visibility.VISIBLE),
                isDescendantOfA(
                    anyOf(
                        isAssignableFrom(ScrollView::class.java),
                        isAssignableFrom(HorizontalScrollView::class.java),
                        isAssignableFrom(NestedScrollView::class.java)
                    )
                )
            )
        }

        override fun getDescription(): String {
            return "scrollTo"
        }

        override fun perform(
            uiController: UiController,
            view: View
        ) {
            ScrollToAction().perform(uiController, view)
        }
    }

    private fun withBottomNavItemCheckedStatus(isChecked: Boolean): Matcher<View?> {
        return object : BoundedMatcher<View?, BottomNavigationItemView>(
            BottomNavigationItemView::class.java
        ) {
            var triedMatching = false
            override fun describeTo(description: Description) {
                if (triedMatching) {
                    description.appendText("with BottomNavigationItem check status: $isChecked")
                    description.appendText("But was: ${!isChecked}")
                }
            }

            override fun matchesSafely(item: BottomNavigationItemView): Boolean {
                triedMatching = true
                return item.itemData.isChecked == isChecked
            }
        }
    }
}

