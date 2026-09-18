package com.example.dailyfocus.ai

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    abstract fun bindAiExtractionEngine(engine: GeminiNanoExtractionEngine): AiExtractionEngine
}
