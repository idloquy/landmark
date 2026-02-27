package com.idloquy.landmark.di

import android.content.Context
import androidx.room.Room
import com.idloquy.landmark.data.database.LandmarkDatabase
import com.idloquy.landmark.data.database.dao.MarkDao
import com.idloquy.landmark.data.database.dao.SharedMarkGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideLandmarkDatabase(@ApplicationContext context: Context): LandmarkDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = LandmarkDatabase::class.java,
            name = "data.db",
        )
            .build()
    }

    @Provides
    fun provideMarkDao(
        landmarkDatabase: LandmarkDatabase,
    ): MarkDao {
        return landmarkDatabase.markDao()
    }

    @Provides
    fun provideSharedMarkGroupDao(
        landmarkDatabase: LandmarkDatabase,
    ): SharedMarkGroupDao {
        return landmarkDatabase.sharedMarkGroupDao()
    }
}