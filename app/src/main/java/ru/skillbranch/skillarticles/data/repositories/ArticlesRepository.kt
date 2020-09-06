package ru.skillbranch.skillarticles.data.repositories

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.DbManager.db
import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.local.dao.*
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.ArticleTagXRef
import ru.skillbranch.skillarticles.data.local.entities.CategoryData
import ru.skillbranch.skillarticles.data.local.entities.Tag
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.err.NoNetworkError
import ru.skillbranch.skillarticles.data.remote.res.ArticleRes
import ru.skillbranch.skillarticles.extensions.data.toArticle
import ru.skillbranch.skillarticles.extensions.data.toArticleContent
import ru.skillbranch.skillarticles.extensions.data.toArticleCounts
import ru.skillbranch.skillarticles.extensions.data.toCategory

interface IArticlesRepository {
    fun findTags(): LiveData<List<String>>
    fun findCategoriesData(): LiveData<List<CategoryData>>
    fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem>
    suspend fun incrementTagUseCount(tag: String)
    suspend fun loadArticlesFromNetwork(start: String? = null, size: Int = 10): Int
    suspend fun toggleBookmark(articleId: String): Boolean
    suspend fun findLastArticleId(): String?
    suspend fun fetchArticleContent(articleId: String)
    suspend fun removeArticleContent(articleId: String)
}

object ArticlesRepository : IArticlesRepository {

    private val network = NetworkManager.api
    private val prefs = PrefManager
    private var articlesDao = db.articlesDao()
    private var articlesContentDao = db.articleContentsDao()
    private var articleCountsDao = db.articleCountsDao()
    private var categoriesDao = db.categoriesDao()
    private var tagsDao = db.tagsDao()
    private var articlePersonalDao = db.articlePersonalInfosDao()

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun setupTestDao(
        articlesDao: ArticlesDao,
        articleCountsDao: ArticleCountsDao,
        categoriesDao: CategoriesDao,
        tagsDao: TagsDao,
        articlePersonalDao: ArticlePersonalInfosDao,
        articlesContentDao :ArticleContentsDao
    ) {
        this.articlesDao = articlesDao
        this.articleCountsDao = articleCountsDao
        this.categoriesDao = categoriesDao
        this.tagsDao = tagsDao
        this.articlePersonalDao = articlePersonalDao
        this.articlesContentDao = articlesContentDao
    }

    override fun findTags(): LiveData<List<String>> {
        return tagsDao.findTags()
    }

    override fun findCategoriesData(): LiveData<List<CategoryData>> {
        return categoriesDao.findAllCategoriesData()
    }

    override fun rawQueryArticles(filter: ArticleFilter): DataSource.Factory<Int, ArticleItem> {
        return articlesDao.findArticlesByRaw(SimpleSQLiteQuery(filter.toQuery()))
    }

    override suspend fun incrementTagUseCount(tag: String) {
        tagsDao.incrementTagUseCount(tag)
    }

    override suspend fun loadArticlesFromNetwork(start: String?, size: Int): Int {
        val items = network.articles(start, size)
        if (items.isNotEmpty()) insertArticlesToDb(items)
        return items.size
    }

    override suspend fun toggleBookmark(articleId: String): Boolean {
        return articlePersonalDao.toggleBookmarkOrInsert(articleId)
    }

    override suspend fun findLastArticleId(): String? = articlesDao.findLastArticleId()

    private suspend fun insertArticlesToDb(articles: List<ArticleRes>) {
        articlesDao.upsert(articles.map { it.data.toArticle() })
        articleCountsDao.upsert(articles.map { it.counts.toArticleCounts() })

        val refs = articles.map { it.data }
            .fold(mutableListOf<Pair<String, String>>()) { acc, res ->
                acc.also { list -> list.addAll(res.tags.map { res.id to it }) }
            }

        val tags = refs.map { it.second }
            .distinct()
            .map { Tag(it) }

        val categories = articles.map { it.data.category.toCategory() }

        categoriesDao.insert(categories)
        tagsDao.insert(tags)
        tagsDao.insertRefs(refs.map { ArticleTagXRef(it.first, it.second) })
    }

    override suspend fun fetchArticleContent(articleId: String) {
        val content = network.loadArticleContent(articleId)
        articlesContentDao.insert(content.toArticleContent())
    }

    override suspend fun removeArticleContent(articleId: String) {
        articlesContentDao.delete(articleId)
    }

    suspend fun addBookmark(articleId: String) {
        if (prefs.accessToken.isEmpty()) return
        try {
            network.addBookmark(articleId, prefs.accessToken)
        } catch (e:Throwable){
            if (e is NoNetworkError) return
            throw e
        }
    }

    suspend fun removeBookmark(articleId: String) {
        if (prefs.accessToken.isEmpty()) return
        try {
            network.removeBookmark(articleId, prefs.accessToken)
        } catch (e:Throwable){
            if (e is NoNetworkError) return
            throw e
        }
    }
}

class ArticleFilter(
    val search: String? = null,
    val isBookmark: Boolean = false,
    val categories: List<String> = listOf(),
    val isHashtag: Boolean = false
) {
    fun toQuery(): String {
        val queryBuilder = QueryBuilder()
        queryBuilder.table("ArticleItem")

        if (search != null && !isHashtag) queryBuilder.appendWhere("title LIKE '%$search%'")
        if (search != null && isHashtag) {
            queryBuilder.innerJoin("article_tag_x_ref AS refs", "refs.a_id = id")
            queryBuilder.appendWhere("refs.t_id = '$search'")
        }
        if (isBookmark) queryBuilder.appendWhere("is_bookmark = 1")
        if (categories.isNotEmpty()) queryBuilder.appendWhere("category_id IN (${categories.joinToString(",")})")

        queryBuilder.orderBy("date")
        return queryBuilder.build()
    }
}

class QueryBuilder {
    private var table: String? = null
    private var selectColumns: String = "*"
    private var joinTables: String? = null
    private var whereCondition: String? = null
    private var order: String? = null

    fun build(): String {
        check(table != null) { "table must not be null" }
        val stringBuilder = StringBuilder("SELECT ")
            .append("$selectColumns ")
            .append("FROM $table")

        if (joinTables != null) stringBuilder.append(joinTables)
        if (whereCondition != null) stringBuilder.append(whereCondition)
        if (order != null) stringBuilder.append(order)

        return stringBuilder.toString()
    }

    fun table(table: String): QueryBuilder {
        this.table = table
        return this
    }

    fun orderBy(column: String, isDesc: Boolean = true): QueryBuilder {
        order = " ORDER BY $column ${ if(isDesc) "DESC" else "ASC" }"
        return this
    }

    fun appendWhere(condition: String, logic: String = " AND"): QueryBuilder {
        if (whereCondition.isNullOrEmpty()) whereCondition = " WHERE $condition"
        else whereCondition += "$logic $condition"

        return this
    }

    fun innerJoin(table: String, on: String): QueryBuilder {
        if (joinTables.isNullOrEmpty()) joinTables = " INNER JOIN $table ON $on"
        else joinTables += " INNER JOIN $table ON $on"

        return this
    }

}