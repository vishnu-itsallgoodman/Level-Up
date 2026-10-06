package com.levelup.app.di

import android.content.Context
import androidx.room.Room
import com.levelup.app.data.database.*
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
    fun provideDatabase(@ApplicationContext context: Context): LevelUpDatabase =
        Room.databaseBuilder(context, LevelUpDatabase::class.java, "levelup.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserProgressDao(db: LevelUpDatabase) = db.userProgressDao()
    @Provides fun provideWorkoutDao(db: LevelUpDatabase) = db.workoutDao()
    @Provides fun provideHabitDao(db: LevelUpDatabase) = db.habitDao()
    @Provides fun provideDailyRecordDao(db: LevelUpDatabase) = db.dailyRecordDao()
    @Provides fun provideWorkoutProgressDao(db: LevelUpDatabase) = db.workoutProgressDao()
    @Provides fun provideHabitProgressDao(db: LevelUpDatabase) = db.habitProgressDao()
    @Provides fun provideAchievementDao(db: LevelUpDatabase) = db.achievementDao()
}
