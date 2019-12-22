package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId: String) : BaseViewModel<ArticleState>(ArticleState()) {
    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()) { article, articleState ->
            article ?: return@subscribeOnDataSource null
            articleState.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()) { content, articleState ->
            content ?: return@subscribeOnDataSource null
            articleState.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()) { personalInfo, articleState ->
            personalInfo ?: return@subscribeOnDataSource null
            articleState.copy(
                isLike = personalInfo.isLike,
                isBookmark = personalInfo.isBookmark
            )
        }

        subscribeOnDataSource(repository.getAppSettings()) { settings, articleState ->
            articleState.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    /* 3 метода, которые используются для подписки на данные */
    // load text from network
    private fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    // from db
    private fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    // from db
    private fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }
    /* 3 метода, которые используются для подписки на данные */

    fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    fun handleNightMode() {
        val setting = currentState.toAppSettings()
        repository.updateSettings(setting.copy(isDarkMode = !setting.isDarkMode))
    }

    fun handleLike() {
        val toggleLike = {
            val info = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }

        toggleLike()

        val notify =
            if (currentState.isLike) Notify.TextMessage("Mark as liked")
            else {
                Notify.ActionMessage("Don't like it anymore", "No, still like it", toggleLike)
            }

        notify(notify)
    }

    fun handleBookmark() {
        // TODO
    }

    fun handleShare() {
        notify(Notify.ErrorMessage("Share is not implemented", "Ok", null))
    }

    fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

}

data class ArticleState(
    val isAuth: Boolean = false,
    val isLoadingContent: Boolean = true,
    val isLoadingReviews: Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu: Boolean = false, // Отображается в меню
    val isBigText: Boolean = false, // Шрифт увеличен
    val isDarkMode: Boolean = false, // Темный режим
    val isSearch: Boolean = false, // В режиме поиска
    val searchQuery: String? = null,
    val searchResults: List<Pair<Int, Int>> = emptyList(), // Результаты поиска (стартовая и конечная позиция)
    val searchPosition: Int = 0, // Текущая позиция найденного результата
    val shareLink: String? = null, // Ссылка share
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val poster: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)