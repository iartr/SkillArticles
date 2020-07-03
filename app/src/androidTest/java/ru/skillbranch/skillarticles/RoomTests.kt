package ru.skillbranch.skillarticles

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.skillbranch.skillarticles.data.local.AppDb
import ru.skillbranch.skillarticles.data.local.DbManager
import ru.skillbranch.skillarticles.data.local.entities.Article
import ru.skillbranch.skillarticles.data.local.entities.Author
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoomTests {
    private lateinit var testDb: AppDb

    @Before
    fun createDb() {
        testDb = DbManager.db
//        testDb = Room.inMemoryDatabaseBuilder(
//            InstrumentationRegistry.getInstrumentation().context,
//            AppDb::class.java
//        ).build()
    }

    @After
    fun closeDb() {
        testDb.close()
    }

    @Test
    fun test_insert_one() {
        val expectedArticle = Article(
            id = "0",
            title = "test article",
            description = "test description",
            categoryId = "0",
            poster = "anyurl",
            date = Date(),
            updatedAt = Date(),
            author = Author(userId = "0", avatar = "avatar url", name = "John Doe")
        )

        testDb.articlesDao().insert(expectedArticle)
        val actual = testDb.articlesDao().findArticleById(expectedArticle.id)

        assertEquals(expectedArticle, actual)
    }

    @Test
    fun test_insert_many() {
        val expectedArticles = mutableListOf<Article>()
        val expectedArticle = Article(
            id = "0",
            title = "test article",
            description = "test description",
            categoryId = "0",
            poster = "anyurl",
            date = Date(),
            updatedAt = Date(),
            author = Author(userId = "0", avatar = "avatar url", name = "John Doe")
        )

        repeat(3) {
            expectedArticles.add(expectedArticle.copy(id = "$it"))
        }

        testDb.articlesDao().insert(expectedArticle.copy(title = "insert first"))
        testDb.articlesDao().upsert(expectedArticles)
        val actualArticles = testDb.articlesDao().findArticles()
        assertEquals(expectedArticles, actualArticles)
    }

    // TODO - из лекции
    @Test
    fun test_insert_many_with_counts() {

    }

    // TODO - из лекции
    @Test
    fun test_foreign_key_with_counts() {

    }

    // TODO - из лекции
    @Test
    fun test_update_counts() {

    }

    // TODO - из лекции
    @Test
    fun test_items_by_category() {

    }

    // TODO - из лекции
    @Test
    fun test_toggle_or_insert() {

    }

    // TODO - из лекции
    @Test
    fun test_tag_increment_use_count() {

    }

    // TODO - из лекции
    @Test
    fun test_find_article_items_by_tag() {

    }

    // TODO - из лекции
    @Test
    fun test_find_article_items_by_raw() {

    }
}