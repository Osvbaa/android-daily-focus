package com.example.dailyfocus.core.testing.analytics

import com.example.dailyfocus.core.common.analytics.AnalyticsEvent
import com.example.dailyfocus.core.common.analytics.AnalyticsTracker

class FakeAnalyticsTracker : AnalyticsTracker {

    private val _events = mutableListOf<AnalyticsEvent>()
    val events: List<AnalyticsEvent> get() = _events.toList()

    override fun track(event: AnalyticsEvent) {
        _events.add(event)
    }

    fun hasDispatched(eventName: String): Boolean = _events.any { it.name == eventName }

    fun countOf(eventName: String): Int = _events.count { it.name == eventName }

    fun clear() {
        _events.clear()
    }
}
