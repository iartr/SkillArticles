package ru.skillbranch.skillarticles

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jraska.livedata.test
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import ru.skillbranch.skillarticles.data.*
import ru.skillbranch.skillarticles.extensions.format
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    val testRule = InstantTaskExecutorRule()

    @Test
    fun A_viewModel_subscriptions_on_data_source() {
        val vm = ArticleViewModel("0")

        vm.state.test()
            .awaitValue() //load app settings
            .assertValue { it.isLoadingContent == true }
            .awaitNextValue() //load article personal info
            .assertValue { it.isLoadingContent == true && it.isBookmark == true }
            .awaitNextValue() //load article info
            .assertValue {
                it.isLoadingContent == true &&
                        it.isBookmark == true &&
                        it.title == "CoordinatorLayout Basic" &&
                        it.category == "Android" &&
                        it.author == "Skill-Branch"
            }
            .awaitNextValue()//load article content
            .assertValue {
                it.isLoadingContent == false &&
                        it.isBookmark == true &&
                        it.title == "CoordinatorLayout Basic" &&
                        it.category == "Android" &&
                        it.author == "Skill-Branch" &&
                        it.content.first() == longText
            }
            .assertHistorySize(4)

        //change app settings
        LocalDataHolder.settings.value = AppSettings(isDarkMode = true)

        vm.state.test()
            .awaitValue()
            .assertValue { it.isDarkMode == true }

        //change personal info
        LocalDataHolder.articleInfo.value = ArticlePersonalInfo(isLike = true, isBookmark = false)

        vm.state.test()
            .awaitValue()
            .assertValue {
                it.isLike == true &&
                        it.isBookmark == false
            }

        //change article data
        val expectedDate = Date()
        LocalDataHolder.articleData.value = ArticleData(
            title = "test title",
            category = "test",
            author = "test",
            shareLink = "any share link",
            date = expectedDate
        )

        vm.state.test()
            .awaitValue()
            .assertValue {
                it.title == "test title" &&
                it.category == "test" &&
                it.author == "test" &&
                it.shareLink == "any share link" &&
                it.date == expectedDate.format()
            }

        //change content data
        NetworkDataHolder.content.value = listOf("long long text content")

        vm.state.test()
            .awaitValue()
            .assertValue {
                it.content.first() == "long long text content"
            }
    }

    @Test
    fun B_viewModel_actions_implementation() {
        LocalDataHolder.disableDelay()
        NetworkDataHolder.disableDelay()
        val vm = ArticleViewModel("0")

        //load init data
        vm.state.test()
            .awaitValue()
            .assertHasValue()

        //clear state
        vm.state.value = ArticleState()
        vm.state.test()
            .awaitValue()
            .assertValue(ArticleState())

        //like check
        vm.handleLike()
        vm.state
            .test()
            .awaitValue()
            .assertValue {
                it.isLike == true
            }

        vm.notifications
            .test()
            .awaitValue()
            .assertValue {
                it.peekContent().message == "Mark as liked"
            }

        //like uncheck
        vm.handleLike()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isLike == false }

        vm.notifications
            .test()
            .awaitValue()
            .assertValue {
                val (msg, label, _) = (it.peekContent() as Notify.ActionMessage)
                msg == "Don`t like it anymore" &&
                label == "No, still like it"
            }

        //check Bookmark
        vm.handleBookmark()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isBookmark == true }

        vm.notifications
            .test()
            .awaitValue()
            .assertValue {
                it.peekContent().message == "Add to bookmarks"
            }

        //uncheck Bookmark
        vm.handleBookmark()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isBookmark == false }

        vm.notifications
            .test()
            .awaitValue()
            .assertValue {
                it.peekContent().message == "Remove from bookmarks"
            }

        vm.handleUpText()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isBigText == true }

        vm.handleDownText()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isBigText == false }

        vm.handleNightMode()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isDarkMode == true }


        vm.handleToggleMenu()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isShowMenu == true }

        vm.handleToggleMenu()
        vm.state
            .test()
            .awaitValue()
            .assertValue { it.isShowMenu == false }

        vm.handleShare()
        vm.notifications
            .test()
            .awaitValue()
            .assertValue {
                val (msg, label, handler) = (it.peekContent() as Notify.ErrorMessage)
                msg == "Share is not implemented" &&
                label == "OK" &&
                handler == null

            }

    }

}


