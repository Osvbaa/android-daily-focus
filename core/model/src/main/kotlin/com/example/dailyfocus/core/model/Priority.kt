package com.example.dailyfocus.core.model

/**
 * Universal priority for tasks, projects, and goals.
 * Defined in the Domain-Driven Design (DDD) model.
 */
enum class Priority {
    NONE,
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    companion object {
        val DEFAULT = NORMAL
    }
}
