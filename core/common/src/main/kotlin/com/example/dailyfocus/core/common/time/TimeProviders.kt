package com.example.dailyfocus.core.common.time

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

fun interface DateProvider { fun today(): LocalDate }
fun interface WallClock { fun currentTimeMillis(): Long }
fun interface MonotonicClock { fun elapsedRealtimeMillis(): Long }
fun interface IdGenerator { fun nextId(): String }

class SystemDateProvider @Inject constructor() : DateProvider { override fun today() = LocalDate.now() }
class SystemWallClock @Inject constructor() : WallClock { override fun currentTimeMillis() = System.currentTimeMillis() }
class SystemMonotonicClock @Inject constructor() : MonotonicClock { override fun elapsedRealtimeMillis() = System.nanoTime() / 1_000_000L }
class UuidGenerator @Inject constructor() : IdGenerator { override fun nextId() = UUID.randomUUID().toString() }
