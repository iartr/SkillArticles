package ru.skillbranch.skillarticles.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import ru.skillbranch.skillarticles.data.local.entities.Category
import ru.skillbranch.skillarticles.data.local.entities.CategoryData

@Dao
interface CategoriesDao : BaseDao<Category> {

    @Query("""
        SELECT category.title AS title, category.icon, category.category_id AS category_id, COUNT(article.category_id) AS articles_count 
        FROM article_categories AS category
        INNER JOIN articles AS article ON category.category_id = article.category_id
        GROUP BY category.category_id
        ORDER BY articles_count DESC
    """)
    fun findAllCategoriesData(): LiveData<List<CategoryData>>
}