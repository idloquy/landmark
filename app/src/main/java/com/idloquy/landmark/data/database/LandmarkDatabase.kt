package com.idloquy.landmark.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.dao.SharedMarkGroupDao
import com.idloquy.landmark.data.database.model.Mark
import com.idloquy.landmark.data.database.model.SharedMark
import com.idloquy.landmark.data.database.model.SharedMarkGroup

@Database(
    entities = [Mark::class, SharedMarkGroup::class, SharedMark::class],
    version = 2,
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2
        )
    ],
    exportSchema = true,
)
abstract class LandmarkDatabase : RoomDatabase() {
    abstract fun markDao(): MarkDao
    abstract fun sharedMarkGroupDao(): SharedMarkGroupDao
}