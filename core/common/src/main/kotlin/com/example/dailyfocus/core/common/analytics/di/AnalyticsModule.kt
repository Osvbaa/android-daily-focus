package com.example.dailyfocus.core.common.analytics.di

import com.example.dailyfocus.core.common.analytics.AnalyticsEvent
import com.example.dailyfocus.core.common.analytics.AnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class LocalAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun track(event: AnalyticsEvent) = Unit
}

@Module @InstallIn(SingletonComponent::class)
interface AnalyticsModule {
    @Binds fun bindAnalyticsTracker(impl: LocalAnalyticsTracker): AnalyticsTracker
}
