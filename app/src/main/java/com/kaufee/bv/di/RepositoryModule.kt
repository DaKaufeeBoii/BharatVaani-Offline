package com.kaufee.bv.di

import com.kaufee.bv.data.repository.TranslationRepositoryImpl
import com.kaufee.bv.domain.repository.TranslationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTranslationRepository(
        impl: TranslationRepositoryImpl
    ): TranslationRepository
}

