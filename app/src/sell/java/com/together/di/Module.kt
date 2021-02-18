package com.together.di

import com.together.base.DataRepositorySell
import com.together.base.DataRepositorySellImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun providesDataRepository(impl: DataRepositorySellImpl): DataRepositorySell
}


//@Module
//@InstallIn(ViewModelComponent::class)
//object RepositoryModule {
//
//    @Provides
//    @ViewModelScoped
//    fun providesDataRepository(impl: DataRepositorySellImpl): DataRepositorySell = impl
//}
