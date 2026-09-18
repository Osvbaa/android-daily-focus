package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.FocusSession
import kotlinx.coroutines.flow.Flow

/** Coordinates the optional target action after a completed focus session. */
interface FocusTargetDecisionRepository {
    fun observePending(): Flow<FocusSession?>
    /** Returns false when the decision has already been resolved or the session is ineligible. */
    suspend fun confirm(sessionId: String): Result<Boolean>
    suspend fun dismiss(sessionId: String): Result<Boolean>
}
