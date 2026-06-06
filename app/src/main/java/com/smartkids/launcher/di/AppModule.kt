package com.smartkids.launcher.di

import android.content.Context
import androidx.room.Room
import com.smartkids.launcher.data.local.AppDao
import com.smartkids.launcher.data.local.AppDatabase
import com.smartkids.launcher.data.AppRepositoryImpl
import com.smartkids.launcher.domain.repository.AppRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "smartkids_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideAppDao(db: AppDatabase): AppDao = db.appDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppRepository(impl: AppRepositoryImpl): AppRepository
}