package com.example.points.di

import com.example.points.ui.point.PointsFragment
import com.example.points.ui.search.SearchFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributePointsFragment(): PointsFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment
}
