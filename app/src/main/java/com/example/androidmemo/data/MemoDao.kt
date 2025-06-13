package com.example.androidmemo.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY date DESC")
    fun getAllMemos(): LiveData<List<Memo>>

    @Query("SELECT * FROM memos WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchMemos(query: String): LiveData<List<Memo>>

    @Insert
    suspend fun insert(memo: Memo): Long

    @Update
    suspend fun update(memo: Memo)

    @Delete
    suspend fun delete(memo: Memo)

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getMemoById(id: Long): Memo?
} 