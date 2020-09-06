package ru.skillbranch.skillarticles.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<T : Any> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(list: List<T>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(obj: T): Long

    @Update
    suspend fun update(list: List<T>)

    @Update
    suspend fun update(obj: T)
}