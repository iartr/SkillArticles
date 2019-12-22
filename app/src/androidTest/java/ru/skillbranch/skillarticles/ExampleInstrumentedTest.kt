package ru.skillbranch.skillarticles

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
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
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    @UiThreadTest
    fun viewModel_subscriptions_on_data_source() {
        val expectedDate = Date()
        LocalDataHolder.articleData.value =
            ArticleData(title = "test article", category = "android", date = expectedDate)
        val expected = ArticleState(
            false,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            emptyList(),
            0,
            null,
            "test article",
            "android",
            null,
            expectedDate.format(),
            null,
            null,
            emptyList(),
            emptyList()
        )

        val expectedPass1 = expected.copy(isLike = true, isBookmark = true)
        val expectedPass2 = expectedPass1.copy(isDarkMode = true)
        val expectedPass3 = expectedPass2.copy(content = listOf("long long text"), isLoadingContent = false)

        val vm = ArticleViewModel("0")

        vm.state.observeOnce {
            assertEquals(expected, it)
        }

        LocalDataHolder.articleInfo.value = ArticlePersonalInfo(isLike = true, isBookmark = true)
        vm.state.observeOnce {
            assertEquals(expectedPass1, it)
        }

        LocalDataHolder.settings.value = AppSettings(isDarkMode = true)
        vm.state.observeOnce {
            assertEquals(expectedPass2, it)
        }

        NetworkDataHolder.content.value = listOf("long long text")
        vm.state.observeOnce {
            assertEquals(expectedPass3, it)
        }
    }

    @Test
    @UiThreadTest
    fun viewModel_actions_implementation() {
        val vm = ArticleViewModel("0")
        LocalDataHolder.disableDelay()
        vm.handleLike()
        vm.state.observeOnce {
            assertEquals(true, it.isLike)
        }

        vm.handleLike()
        vm.state.observeOnce {
            assertEquals(false, it.isLike)
        }

        vm.handleBookmark()
        vm.state.observeOnce {
            assertEquals(true, it.isBookmark)
        }
        vm.notifications.observeOnce {
            assertEquals("Add to bookmarks", it.peekContent().message)
        }

        vm.handleBookmark()
        vm.state.observeOnce {
            assertEquals(false, it.isBookmark)
        }


        vm.handleUpText()
        vm.state.observeOnce {
            assertEquals(true, it.isBigText)
        }

        vm.handleDownText()
        vm.state.observeOnce {
            assertEquals(false, it.isBigText)
        }

        vm.handleNightMode()
        vm.state.observeOnce {
            assertEquals(true, it.isDarkMode)
        }

        vm.handleToggleMenu()
        vm.state.observeOnce {
            assertEquals(true, it.isShowMenu)
        }

        vm.handleToggleMenu()
        vm.state.observeOnce {
            assertEquals(false, it.isShowMenu)
        }

        vm.handleShare()
        vm.notifications.observeOnce {
            assertEquals(
                Notify.ErrorMessage("Share is not implemented", "OK", null),
                it.peekContent()
            )
        }
    }
}

class OneTimeObserver<T>(private val handler: (T) -> Unit) :
    Observer<T>,
    LifecycleOwner {
    private val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun getLifecycle(): Lifecycle = lifecycle

    override fun onChanged(t: T) {
        handler(t)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

fun <T> LiveData<T>.observeOnce(onChangeHandler: (T) -> Unit) {
    val observer = OneTimeObserver(handler = onChangeHandler)
    observe(observer, observer)
}