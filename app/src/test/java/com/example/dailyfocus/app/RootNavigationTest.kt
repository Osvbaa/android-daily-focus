package com.example.dailyfocus.app

import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.notes.navigation.NoteListRoute
import com.example.dailyfocus.features.tasks.navigation.TaskEditorRoute
import com.example.dailyfocus.features.tasks.navigation.TaskRoute
import com.example.dailyfocus.features.today.navigation.TodayRoute
import com.example.dailyfocus.features.projects.navigation.ProjectsRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class RootNavigationTest {
    @Test
    fun switchingRootsKeepsPreviousContextForBackNavigation() {
        val stack = mutableListOf<NavKey>(TodayRoute, TaskEditorRoute("task-1"))

        selectRoot(stack, NoteListRoute)

        assertEquals(listOf(TodayRoute, TaskEditorRoute("task-1"), NoteListRoute), stack)
    }

    @Test
    fun selectingCurrentRootDoesNotDuplicateIt() {
        val stack = mutableListOf<NavKey>(TodayRoute, TaskRoute)

        selectRoot(stack, TaskRoute)

        assertEquals(listOf(TodayRoute, TaskRoute), stack)
    }

    @Test
    fun returningToPreviousRootMovesItToTopWithoutDuplicating() {
        val stack = mutableListOf<NavKey>(TodayRoute, TaskRoute, NoteListRoute)

        selectRoot(stack, TodayRoute)

        assertEquals(listOf(TaskRoute, NoteListRoute, TodayRoute), stack)
    }

    @Test
    fun projectTaskDetailKeepsProjectOnBackStack() {
        val stack = mutableListOf<NavKey>(TodayRoute, MoreRoute, ProjectsRoute)

        openProjectTask(stack, "project-task")

        assertEquals(ProjectsRoute, stack[stack.lastIndex - 1])
        assertEquals(TaskEditorRoute("project-task"), stack.last())
    }
}
