package ru.skillbranch.skillarticles.viewmodels.articles

import androidx.lifecycle.*
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import ru.skillbranch.skillarticles.data.repositories.ArticlesRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
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
    private val listData: LiveData<PagedList<ArticleItemData>> = Transformations.switchMap(state) {
        if (it.isSearch && !it.searchQuery.isNullOrBlank()) buildPagedList(repository.searchArticles(it.searchQuery))
        else buildPagedList(repository.allArticles())
    }

    fun observeList(owner: LifecycleOwner, onChange: (list: PagedList<ArticleItemData>) -> Unit) {
        listData.observe(owner, Observer { onChange(it) })
    }

    private fun buildPagedList(dataFactory: DataSource.Factory<Int, ArticleItemData>): LiveData<PagedList<ArticleItemData>> {
        return LivePagedListBuilder<Int, ArticleItemData>(dataFactory, listConfig)
            .setFetchExecutor(Executors.newSingleThreadExecutor())
            .build()
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
}

data class ArticlesState(
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = true
): IViewModelState