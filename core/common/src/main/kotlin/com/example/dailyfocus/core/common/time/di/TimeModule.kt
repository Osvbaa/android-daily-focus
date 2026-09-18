package com.example.dailyfocus.core.common.time.di

import com.example.dailyfocus.core.common.time.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module @InstallIn(SingletonComponent::class)
interface TimeModule {
    @Binds fun bindDateProvider(impl: SystemDateProvider): DateProvider
    @Binds fun bindWallClock(impl: SystemWallClock): WallClock
    @Binds fun bindMonotonicClock(impl: SystemMonotonicClock): MonotonicClock
    @Binds fun bindIdGenerator(impl: UuidGenerator): IdGenerator
}
