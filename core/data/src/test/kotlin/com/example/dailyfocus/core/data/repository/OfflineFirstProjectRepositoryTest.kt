package com.example.dailyfocus.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.model.TaskDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineFirstProjectRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var projects: OfflineFirstProjectRepository
    private lateinit var tasks: OfflineFirstTaskRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java,
        ).build()
        var taskSequence = 0
        tasks = OfflineFirstTaskRepository(
            database.taskDao(), IdGenerator { "task-${++taskSequence}" },
            WallClock { 100L }, Dispatchers.IO, database.projectDao(),
        )
        var projectSequence = 0
        projects = OfflineFirstProjectRepository(
            database.projectDao(), tasks, IdGenerator { "project-${++projectSequence}" },
            WallClock { 100L }, Dispatchers.IO,
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `duplicate milestone titles after trimming reject entire project`() = runBlocking {
        val result = projects.createProject(ProjectDraft("Lanzamiento", milestones = listOf("Entrega", " Entrega ")))

        assertTrue(result is ProjectMutationResult.Failure)
        assertTrue(projects.observeProjects().first().isEmpty())
    }

    @Test
    fun `archiving preserves linked task and milestone`() = runBlocking {
        val created = projects.createProject(ProjectDraft("Lanzamiento", milestones = listOf(" Primera ")))
        val projectId = (created as ProjectMutationResult.Success).projectId
        val milestoneId = projects.observeProject(projectId).first()!!.milestones.single().id
        val task = tasks.saveTask(null, TaskDraft(title = "Publicar", projectId = projectId, milestoneId = milestoneId))
        assertTrue(task is TaskMutationResult.Success)

        assertTrue(projects.archiveProject(projectId) is ProjectMutationResult.Success)

        val details = projects.observeProject(projectId).first()!!
        assertTrue(details.project.isArchived)
        assertEquals("Primera", details.milestones.single().title)
        assertEquals(projectId, details.tasks.single().projectId)
        assertEquals(milestoneId, details.tasks.single().milestoneId)
        assertTrue(projects.activeProjects().isEmpty())
    }

    @Test
    fun `task cannot use a milestone from another project`() = runBlocking {
        val first = (projects.createProject(ProjectDraft("Uno", milestones = listOf("Hito"))) as ProjectMutationResult.Success).projectId
        val second = (projects.createProject(ProjectDraft("Dos")) as ProjectMutationResult.Success).projectId
        val foreignMilestone = projects.observeProject(first).first()!!.milestones.single().id

        val result = tasks.saveTask(null, TaskDraft(title = "Tarea", projectId = second, milestoneId = foreignMilestone))

        assertTrue(result is TaskMutationResult.Failure)
        assertTrue(tasks.observeTasks().first().isEmpty())
    }
}
