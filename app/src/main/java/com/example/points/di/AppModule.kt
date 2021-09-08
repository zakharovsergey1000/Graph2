package com.example.points.di

import android.app.Application
import androidx.room.Room
import com.example.points.api.PointsService
import com.example.points.api.UnsafeOkHttpClient
import com.example.points.db.PointsDb
import com.example.points.db.PointDao
import com.example.points.util.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Singleton
    @Provides
    fun provideGithubService(): PointsService {
        val okHttpClient = UnsafeOkHttpClient.getUnsafeOkHttpClient().newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .cache(null)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://hr-challenge.interactivestandard.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(PointsService::class.java)
    }

    @Singleton
    @Provides
    fun provideDb(app: Application): PointsDb {
        return Room
            .databaseBuilder(app, PointsDb::class.java, "github.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providePointDao(db: PointsDb): PointDao {
        return db.pointDao()
    }
}
