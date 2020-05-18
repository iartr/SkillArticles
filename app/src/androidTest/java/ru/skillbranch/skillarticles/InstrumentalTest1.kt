package ru.skillbranch.skillarticles

import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.widget.NestedScrollView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.AnyOf.anyOf
import org.junit.After
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.articles.ArticleVH
import java.lang.Thread.sleep

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class InstrumentalTest1 {
    @get:Rule
    var activityRule: ActivityTestRule<RootActivity> = ActivityTestRule(RootActivity::class.java)

    @After
    fun beforeTest(){
        LocalDataHolder.localArticleItems.clear()
        activityRule.finishActivity()
    }

    @Test
    fun a_check_page_load() {
        sleep(2000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            assertEquals(
                "RecycleView ArticleItemView count",
                50,
                rv.adapter!!.itemCount
            )

        }

        onView(withId(R.id.rv_articles))
            .perform(RecyclerViewActions.scrollToPosition<ArticleVH>(35))

        sleep(3000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            assertEquals(
                "RecycleView scroll to position 35 lastKey",
                33,
                (rv.adapter as PagedListAdapter<*, *>).currentList?.lastKey
            )

        }
        onView(withId(R.id.rv_articles))
            .perform(RecyclerViewActions.scrollToPosition<ArticleVH>(50))
        sleep(3000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            assertEquals(
                "RecycleView scroll to position 50 lastKey",
                58,
                (rv.adapter as PagedListAdapter<*, *>).currentList?.lastKey
            )
        }
    }

    @Test
    fun b_check_search() {
        sleep(2000)
        onView(withId(R.id.action_search))
            .perform(click())
        onView(withId(R.id.search_view))
            .perform(submitText("draw"))
        sleep(1000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            assertEquals(
                "RecycleView ArticleItemView count",
                9,
                rv.adapter!!.itemCount
            )
        }
    }

    @Test
    fun c_check_bookmark() {
        sleep(2000)
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    5,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )
        sleep(5000)
        onView(withId(R.id.rv_articles))
            .check(
                matches(
                    atPositionOnView(5, allOf(isChecked()), R.id.iv_bookmark)
                )
            )
    }

    @Test
    fun d_check_add_bookmark() {
        sleep(2000)
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    5,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    6,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    7,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )

        onView(withId(R.id.nav_bookmarks))
            .perform(click())

        sleep(500)

        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_bookmarks)
            assertEquals(
                "RecycleView ArticleItemView in bookmarks count",
                3,
                rv.adapter!!.itemCount
            )
        }

        onView(withId(R.id.rv_bookmarks))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    1,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )

        sleep(500)

        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_bookmarks)
            assertEquals(
                "RecycleView ArticleItemView in bookmarks count after delete bookmark",
                2,
                rv.adapter!!.itemCount
            )
        }

    }

    @Test
    fun e_check_search_bookmark() {
        sleep(2000)
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    5,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    6,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    7,
                    clickChildViewWithId(R.id.iv_bookmark)
                )
            )

        onView(withId(R.id.nav_bookmarks))
            .perform(click())

        sleep(500)

        onView(withId(R.id.action_search))
            .perform(click())
        onView(withId(R.id.search_view))
            .perform(submitText("draw"))
        sleep(1000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_bookmarks)
            assertEquals(
                "RecycleView ArticleItemView count in bookmarks after search",
                1,
                rv.adapter!!.itemCount
            )
        }
    }

    @Test
    fun f_check_comments() {
        sleep(2000)
        val commentCount = activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_articles)
            val vh = rv.findViewHolderForAdapterPosition(0) as ArticleVH
            vh.itemView.findViewById<TextView>(R.id.tv_comments_count).text.toString().toInt()
        }
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    0,
                    click()
                )
            )
        sleep(5000)
        activityRule.activity.run {
            val rv = findViewById<RecyclerView>(R.id.rv_comments)
            assertEquals(
                "RecycleView CommentItemView in Article count ",
                commentCount,
                rv.adapter!!.itemCount
            )
        }
    }

    @Test
    fun g_check_comment_send() {
        sleep(2000)
        onView(withId(R.id.rv_articles))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    0,
                    click()
                )
            )
        sleep(5000)
        onView(withId(R.id.et_comment))
            .perform(customScrollTo(), replaceText("comment text"), pressImeActionButton())
        sleep(1000)
        onView(withId(R.id.btn_login))
            .perform(click())
        sleep(1000)

        onView(withId(R.id.et_comment))
            .perform(customScrollTo(), click(), pressImeActionButton())
        sleep(1000)
        onView(withId(R.id.rv_comments))
            .check(
                matches(
                    atPositionOnView(0, allOf(withText("comment text")), R.id.tv_comment_body)
                )
            )
        sleep(5000)
        onView(withId(R.id.rv_comments))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<ArticleVH>(
                    1,
                    click()
                )
            )
        sleep(1000)
        onView(withId(R.id.et_comment))
            .perform(customScrollTo(), replaceText("reply text"), pressImeActionButton())
        sleep(5000)
        onView(withId(R.id.rv_comments))
            .check(
                matches(
                    atPositionOnView(0, allOf(withText("comment text")), R.id.tv_comment_body)
                )
            )
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


fun submitText(text: String): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
        }

        override fun getDescription(): String {
            return "Set text and submit"
        }

        override fun perform(uiController: UiController, view: View) {
            (view as SearchView).setQuery(text, true) //submit=true will fire search
        }
    }
}

fun typeText(text: String): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
        }

        override fun getDescription(): String {
            return "Set text"
        }

        override fun perform(uiController: UiController, view: View) {
            (view as SearchView).setQuery(text, false)
        }
    }
}

fun clickChildViewWithId(id: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return isDisplayed()
        }

        override fun getDescription(): String {
            return "Click on a child view with specified id."
        }

        override fun perform(
            uiController: UiController,
            view: View
        ) {
            val v = view.findViewById<View>(id)
            v.performClick()
        }
    }
}

fun atPositionOnView(position: Int, itemMatcher: Matcher<View>, targetViewId: Int): Matcher<View> {

    return object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("with $position child of RecyclerView not checked")
        }

        override fun matchesSafely(view: View): Boolean {
            if (view !is RecyclerView) {
                return false
            }

            val viewHolder = view.findViewHolderForAdapterPosition(position);
            val targetView = viewHolder?.itemView?.findViewById<View>(targetViewId);
            return if (targetView != null) itemMatcher.matches(targetView) else false
        }
    }
}

