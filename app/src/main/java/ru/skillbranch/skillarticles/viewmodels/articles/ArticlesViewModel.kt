package ru.skillbranch.skillarticles.viewmodels.articles

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.repositories.ArticleFilter
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import java.util.concurrent.Executors

class ArticlesViewModel(handle: SavedStateHandle) : BaseViewModel<ArticlesState>(handle, ArticlesState()) {
    private val repository = ArticlesRepository
    private val listConfig by lazy {
        PagedList.Config.Builder()
            .setEnablePlaceholders(false) // Не известен конечный список данных
            .setPageSize(10)
            .setPrefetchDistance(30)
            .setInitialLoadSizeHint(50)
            .build()
    }
    private val listData: LiveData<PagedList<ArticleItem>> = Transformations.switchMap(state) {
        val filter = it.toArticleFilter()
        return@switchMap buildPagedList(repository.rawQueryArticles(filter))
    }

    fun observeList(owner: LifecycleOwner, isBookmark: Boolean = false, onChange: (list: PagedList<ArticleItem>) -> Unit) {
        updateState { it.copy(isBookmark = isBookmark) }
        listData.observe(owner, Observer { onChange(it) })
    }

    fun observeTags(owner: LifecycleOwner, onChange: (list: List<String>) -> Unit) {
        repository.findTags().observe(owner, Observer(onChange))
    }

    fun observeCategories(owner: LifecycleOwner, onChange: (list: List<CategoryData>) -> Unit) {
        repository.findCategoriesData().observe(owner, Observer(onChange))
    }

    fun handleSearch(query: String?) {
        query ?: return
        updateState {
            it.copy(searchQuery = query)
        }
    }

    fun handleSearchMode(isSearch: Boolean) {
        updateState {
            it.copy(isSearch = isSearch)
        }
    }

    fun handleToggleBookmark(articleId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleBookmark(articleId)
        }
    }

    fun handleSuggestion(tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.incrementTagUseCount(tag)
        }
    }

    fun applyCategories(selectedCategories: List<String>) {
        updateState { it.copy(selectedCategories = selectedCategories) }
    }

    private fun buildPagedList(dataFactory: DataSource.Factory<Int, ArticleItem>): LiveData<PagedList<ArticleItem>> {
        val builder = LivePagedListBuilder<Int, ArticleItem>(dataFactory, listConfig)

        if (isEmptyFilter()) {
            builder.setBoundaryCallback(ArticlesBoundaryCallback(::zeroLoadingHandle, ::itemAtEndHandle))
        }

        return builder.setFetchExecutor(Executors.newSingleThreadExecutor()).build()
    }

    private fun isEmptyFilter(): Boolean {
        return currentState.searchQuery.isNullOrEmpty()
                && !currentState.isBookmark
                && currentState.selectedCategories.isEmpty()
                && !currentState.isHashtagSearch
    }

    private fun zeroLoadingHandle() {
        notify(Notify.TextMessage("Storage is empty"))
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(0, listConfig.initialLoadSizeHint)
            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }
        }
    }

    private fun itemAtEndHandle(itemAtEnd: ArticleItem) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.loadArticlesFromNetwork(itemAtEnd.id.toInt() + 1, listConfig.pageSize)

            if (items.isNotEmpty()) {
                repository.insertArticlesToDb(items)
                listData.value?.dataSource?.invalidate()
            }

            withContext(Dispatchers.Main) {
                notify(Notify.TextMessage("Loaded from network from ${items.firstOrNull()?.data?.id} to ${items.lastOrNull()?.data?.id}"))
            }
        }
    }
}

private fun ArticlesState.toArticleFilter(): ArticleFilter {
    return ArticleFilter(
        search = searchQuery,
        isBookmark = isBookmark,
        categories = selectedCategories,
        isHashtag = isHashtagSearch
    )
}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true,
    val isBookmark: Boolean = false,
    val selectedCategories: List<String> = emptyList(),
    val isHashtagSearch: Boolean = false
): IViewModelState {
    override fun save(outState: SavedStateHandle) {
        outState.set(::isSearch.name, isSearch)
        outState.set(::searchQuery.name, searchQuery)
        outState.set(::isLoading.name, isLoading)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState {
        return this.copy(
            isSearch = savedState.get<Boolean>(::isSearch.name) ?: false,
            searchQuery = savedState.get<String>(::searchQuery.name),
            isLoading = savedState.get<Boolean>(::isLoading.name) ?: true
        )
    }
}

class ArticlesBoundaryCallback(
    private val zeroLoadingHandle: () -> Unit,
    private val itemAtEndHandle: (itemAtEnd: ArticleItem) -> Unit
) : PagedList.BoundaryCallback<ArticleItem>() {

    override fun onZeroItemsLoaded() {
        zeroLoadingHandle()
    }

    override fun onItemAtEndLoaded(itemAtEnd: ArticleItem) {
        itemAtEndHandle(itemAtEnd)
    }
}