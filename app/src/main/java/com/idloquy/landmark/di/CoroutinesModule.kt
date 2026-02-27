package com.idloquy.landmark.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class IoDispatcher

@Qualifier
annotation class IoScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @IoDispatcher
    @Provides
    @Singleton
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @IoScope
    @Provides
    @Singleton
    fun provideIoScope(@IoDispatcher ioDispatcher: CoroutineDispatcher): CoroutineScope = CoroutineScope(ioDispatcher + SupervisorJob())
}