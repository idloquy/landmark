package com.idloquy.landmark.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.model.Mark

@Database(entities = [Mark::class], version = 1)
abstract class LandmarkDatabase : RoomDatabase() {
    abstract fun markDao(): MarkDao
}