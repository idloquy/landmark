package com.idloquy.landmark.data.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup
import com.idloquy.landmark.data.database.model.SharedMarkGroupWithMarks
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedMarkGroupDao {
    @Query("SELECT * FROM sharedmarkgroup")
    fun getAll(): Flow<List<SharedMarkGroup>>

    @Query("SELECT * from sharedmarkgroup WHERE id = :id")
    fun getSharedMarkGroupByIdFlow(id: String): Flow<SharedMarkGroup?>

    @Query("SELECT * from sharedmarkgroup WHERE id = :id")
    suspend fun getSharedMarkGroupById(id: String): SharedMarkGroup?

    @Transaction
    @Query("SELECT * FROM sharedmarkgroup WHERE id = :id")
    fun getSharedMarkGroupWithMarksById(id: String): SharedMarkGroupWithMarks

    @Transaction
    @Query("SELECT * FROM sharedmarkgroup")
    fun getSharedMarkGroupsWithMarksFlow(): Flow<List<SharedMarkGroupWithMarks>>


    @Transaction
    @Query("SELECT * FROM sharedmarkgroup WHERE id = :id LIMIT 1")
    fun getSharedMarkGroupWithMarksByIdFlow(id: String): Flow<SharedMarkGroupWithMarks?>

    @Query("SELECT * from sharedmark WHERE groupId = :groupId AND id = :id")
    suspend fun getSharedMarkById(groupId: String, id: Int): SharedMark?

    @Query("SELECT * from sharedmark WHERE groupId = :groupId AND id = :id")
    fun getSharedMarkByIdFlow(groupId: String, id: Int): Flow<SharedMark?>

    @Insert
    suspend fun insertAllSharedMarkGroups(vararg group: SharedMarkGroup)

    @Insert
    suspend fun insertAllSharedMarks(shared: List<SharedMark>)

    @Insert
    suspend fun insertAllSharedMarks(vararg mark: SharedMark)

    @Transaction
    suspend fun insertAllSharedMarkGroupsWithMarks(vararg sharedMarkGroup: SharedMarkGroupWithMarks) {
        sharedMarkGroup.forEach {
            insertAllSharedMarkGroups(it.sharedMarkGroup)
            insertAllSharedMarks(it.marks)
        }
    }

    @Update
    suspend fun updateSharedMarks(vararg sharedMark: SharedMark)

    @Transaction
    suspend fun updateSharedMarkGroupMarks(group: SharedMarkGroup, sharedMarks: List<SharedMark>) {
        val markGroupWithMarks = getSharedMarkGroupWithMarksById(group.id)
        Log.d("landmark", "updating group: current marks=${markGroupWithMarks.marks} updated marks=$sharedMarks")
        for (existingMark in markGroupWithMarks.marks) {
            val updatedMark = sharedMarks.firstOrNull { it.remoteId == existingMark.remoteId }
            if (updatedMark == null) {
                // The mark's been deleted remotely.
                deleteSharedMark(group.id, existingMark.id)
            } else {
                val markForUpdate = updatedMark.copy(id = existingMark.id)
                Log.d("landmark", "updating $existingMark to $markForUpdate")
                updateSharedMarks(updatedMark.copy(id = existingMark.id))
            }
        }

        Log.d("landmark", "inserting potentially new marks")
        for (sharedMark in sharedMarks) {
            if (markGroupWithMarks.marks.any { it.remoteId == sharedMark.remoteId }) {
                Log.d("landmark", "ignoring known remote id=${sharedMark.remoteId}")
                continue
            }
            Log.d("landmark", "inserting unknown mark: $sharedMark")
            insertAllSharedMarks(sharedMark)
        }
    }

    @Update
    suspend fun updateSharedMarkGroups(vararg sharedMarkGroup: SharedMarkGroup)

    @Delete
    suspend fun deleteAll(groups: List<SharedMarkGroup>)

    @Delete
    suspend fun deleteAll(vararg group: SharedMarkGroup)

    @Query("DELETE FROM sharedmark WHERE groupId = :groupId AND id = :sharedMarkId")
    suspend fun deleteSharedMark(groupId: String, sharedMarkId: Int)

    @Query("SELECT COUNT(*) FROM sharedmark WHERE groupId = :groupId")
    suspend fun sharedMarksCount(groupId: String): Int

    @Delete
    suspend fun deleteSharedMarkGroup(group: SharedMarkGroup)
}