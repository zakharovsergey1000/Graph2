package com.example.points.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.points.ui.point.PointsViewModel
import com.example.points.ui.search.SearchViewModel
import com.example.points.viewmodel.PointsViewModelFactory

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindSearchViewModel(searchViewModel: SearchViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PointsViewModel::class)
    abstract fun bindRepoViewModel(pointsViewModel: PointsViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: PointsViewModelFactory): ViewModelProvider.Factory
}
