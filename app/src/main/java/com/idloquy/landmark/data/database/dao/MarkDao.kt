package com.idloquy.landmark.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.idloquy.landmark.data.database.model.Mark
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkDao {
    @Query("SELECT * FROM mark")
    fun getAll(): Flow<List<Mark>>

    @Query("SELECT * FROM mark WHERE id = :id")
    fun getById(id: Int): Flow<Mark?>

    @Insert
    suspend fun insertAll(vararg marks: Mark)

    @Update
    suspend fun updateAll(vararg mark: Mark)

    @Delete
    suspend fun deleteAll(vararg marks: Mark)

    @Delete
    suspend fun deleteAll(marks: List<Mark>)
}