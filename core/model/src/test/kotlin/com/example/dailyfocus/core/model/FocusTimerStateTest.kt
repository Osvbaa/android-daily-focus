package com.example.dailyfocus.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusTimerStateTest {
    @Test fun `start changes idle to running`() {
        assertEquals(FocusTimerPhase.RUNNING, FocusTimerState().start().phase)
    }

    @Test fun `tick decrements only while running`() {
        val state = FocusTimerState(durationSeconds = 2, remainingSeconds = 2).start()
        assertEquals(1, state.tick().remainingSeconds)
        assertEquals(2, state.pause().tick().remainingSeconds)
    }

    @Test fun `last tick completes session`() {
        val result = FocusTimerState(durationSeconds = 1, remainingSeconds = 1).start().tick()
        assertEquals(FocusTimerPhase.COMPLETED, result.phase)
        assertEquals(1, result.completedSessions)
    }
}
