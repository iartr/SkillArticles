package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import ru.skillbranch.skillarticles.data.local.entities.ArticleCounts

@Dao
interface ArticleCountsDao : BaseDao<ArticleCounts> {
    @Transaction
    fun upsert(list: List<ArticleCounts>) {
        insert(list)
            .mapIndexed { index, recordId -> if (recordId == -1L) list[index] else null }
            .filterNotNull()
            .also { if (it.isNotEmpty()) update(it) }
    }

    @Query("SELECT * FROM article_counts")
    fun findArticleCounts(): List<ArticleCounts>

    @Query("""
        UPDATE article_counts SET likes = likes + 1, updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    fun incrementLike(articleId: String): Int

    @Query("""
        UPDATE article_counts SET likes = MAX(0, likes - 1), updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    fun decrementLike(articleId: String): Int

    @Query("""
        UPDATE article_counts SET comments = comments + 1, updated_at = CURRENT_TIMESTAMP 
        WHERE article_id = :articleId
    """)
    fun incrementCommentsCount(articleId: String): Int

    @Query("SELECT comments FROM article_counts WHERE article_id = :articleId")
    fun getCommentsCount(articleId: String): LiveData<Int>
}