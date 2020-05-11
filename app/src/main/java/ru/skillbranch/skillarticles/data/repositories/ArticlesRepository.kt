package ru.skillbranch.skillarticles.data.repositories

import android.util.Log
import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import ru.skillbranch.skillarticles.data.LocalDataHolder
import ru.skillbranch.skillarticles.data.NetworkDataHolder
import ru.skillbranch.skillarticles.data.models.ArticleItemData
import java.lang.Thread.sleep

object ArticlesRepository {
    private val local = LocalDataHolder
    private val network = NetworkDataHolder

    fun allArticles(): ArticlesDataFactory {
        return ArticlesDataFactory(ArticleStrategy.AllArticles(::findArticlesByRange))
    }

    fun searchArticles(searchQuery: String): ArticlesDataFactory {
        return ArticlesDataFactory(ArticleStrategy.SearchArticle(::searchArticlesByTitle, searchQuery))
    }

    fun loadArticlesFromNetwork(start: Int, size: Int): List<ArticleItemData> {
        return network.networkArticleItems
            .drop(start)
            .take(size)
            .apply { sleep(500) }
    }

    fun insertArticlesToDb(articles: List<ArticleItemData>) {
        local.localArticleItems
            .addAll(articles)
            .apply { sleep(100) }
    }

    private fun findArticlesByRange(start: Int, size: Int) = local.localArticleItems
        .drop(start)
        .take(size)

    private fun searchArticlesByTitle(start: Int, size: Int, queryTitle: String): List<ArticleItemData> {
        return local.localArticleItems
            .asSequence()
            .filter { it.title.contains(queryTitle, ignoreCase = true) }
            .drop(start)
            .take(size)
            .toList()
    }
}

class ArticlesDataFactory(val strategy: ArticleStrategy) : DataSource.Factory<Int, ArticleItemData>() {
    override fun create(): DataSource<Int, ArticleItemData> {
        return ArticleDataSource(strategy)
    }
}

class ArticleDataSource(private val strategy: ArticleStrategy) : PositionalDataSource<ArticleItemData>() {
    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ArticleItemData>) {
        val result = strategy.getItems(params.requestedStartPosition, params.requestedLoadSize)
        Log.e("ArticlesRepository", "loadInitial: start = ${params.requestedStartPosition} size = ${params.requestedLoadSize} resultSize = ${result.size}")
        callback.onResult(result, params.requestedStartPosition)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ArticleItemData>) {
        val result = strategy.getItems(params.startPosition, params.loadSize)
        Log.e("ArticlesRepository", "loadRange: start = ${params.startPosition} size = ${params.loadSize} resultSize = ${result.size}")
        callback.onResult(result)
    }
}

sealed class ArticleStrategy {
    abstract fun getItems(start: Int, size: Int): List<ArticleItemData>

    class AllArticles(private val itemProvider: (Int, Int) -> List<ArticleItemData>): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> {
            return itemProvider(start, size)
        }
    }

    class SearchArticle(private val itemProvider: (Int, Int, String) -> List<ArticleItemData>, private val query: String): ArticleStrategy() {
        override fun getItems(start: Int, size: Int): List<ArticleItemData> {
            return itemProvider(start, size, query)
        }
    }

    // TODO — Bookmarks strategy

}