package com.example.androidmemo.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY date DESC")
    fun getAll(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getById(id: Long): Memo?

    @Query("SELECT * FROM memos WHERE title LIKE :query OR content LIKE :query ORDER BY date DESC")
    fun search(query: String): LiveData<List<Memo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memo: Memo)

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)
} 