package com.example.postura.di

import android.content.Context
import com.example.postura.data.pose.PoseDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.Contexts
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule{

    @Singleton
    @Provides
    fun providePoseDetector(
        @ApplicationContext context: Context
    ): PoseDetector {
        return PoseDetector(context)
    }
}