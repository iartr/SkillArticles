package ru.skillbranch.skillarticles.data.local.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "article_categories")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    val categoryId: String,

    val icon: String,

    val title: String
)

@Parcelize
data class CategoryData(
    @ColumnInfo(name = "category_id")
    val categoryId: String,

    val icon: String,

    val title: String,

    @ColumnInfo(name = "articles_count")
    val articlesCount: Int = 0
) : Parcelable