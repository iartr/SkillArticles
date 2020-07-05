package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.ArticleFull
import ru.skillbranch.skillarticles.data.local.entities.ArticleItem
import ru.skillbranch.skillarticles.data.local.entities.ArticleWithShareLink

@Dao
interface ArticlesDao : BaseDao<Article> {

    @Transaction
    fun upsert(list: List<Article>) {
        insert(list)
            .mapIndexed { index, recordId -> if (recordId == -1L) list[index] else null }
            .filterNotNull()
            .also { if (it.isNotEmpty()) update(it) }
    }

    @Query("SELECT * FROM articles")
    fun findArticles(): LiveData<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun findArticleById(id: String): LiveData<Article>

    @Query("SELECT * FROM ArticleItem")
    fun findArticleItems(): LiveData<List<ArticleItem>>

    @Delete
    fun delete(article: Article)

    @Query("SELECT * FROM ArticleItem WHERE category_id IN (:categoryIds)")
    fun findArticleItemsByCategoryIds(categoryIds: List<String>): LiveData<List<ArticleItem>>

    @Query("""
        SELECT * FROM ArticleItem
        INNER JOIN article_tag_x_ref AS refs ON refs.a_id = id
        WHERE refs.t_id = :tag
    """)
    fun findArticlesByTagId(tag: String): LiveData<List<ArticleItem>>

    @RawQuery(observedEntities = [ArticleItem::class])
    fun findArticlesByRaw(simpleSQLiteQuery: SimpleSQLiteQuery): DataSource.Factory<Int, ArticleItem>

    @Query("SELECT * FROM ArticleFull WHERE id = :articleId")
    fun findFullArticle(articleId: String): LiveData<ArticleFull>

    @Query("SELECT * FROM articles WHERE id = :articleId")
    fun findArticleWithShareLink(articleId: String): LiveData<ArticleWithShareLink>
}