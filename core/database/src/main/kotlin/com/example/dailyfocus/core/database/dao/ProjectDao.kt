package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.dailyfocus.core.database.model.ProjectEntity
import com.example.dailyfocus.core.database.model.ProjectMilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY isArchived ASC, createdAtMillis DESC")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeProject(id: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM project_milestones WHERE project_id = :projectId ORDER BY position ASC")
    fun observeMilestones(projectId: String): Flow<List<ProjectMilestoneEntity>>

    @Upsert
    suspend fun upsertProject(project: ProjectEntity)

    @Upsert
    suspend fun upsertMilestone(milestone: ProjectMilestoneEntity)

    /** A project is never exposed without the milestones supplied at creation time. */
    @androidx.room.Transaction
    suspend fun createProjectWithMilestones(
        project: ProjectEntity,
        milestones: List<ProjectMilestoneEntity>,
    ) {
        upsertProject(project)
        for (milestone in milestones) {
            upsertMilestone(milestone)
        }
    }

    @Query("UPDATE projects SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean): Int

    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY createdAtMillis DESC")
    suspend fun getActiveProjects(): List<ProjectEntity>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProject(id: String): ProjectEntity?

    @Query("SELECT * FROM project_milestones WHERE id = :id")
    suspend fun getMilestone(id: String): ProjectMilestoneEntity?

    @Query("SELECT * FROM project_milestones WHERE project_id = :projectId ORDER BY position ASC")
    suspend fun getMilestones(projectId: String): List<ProjectMilestoneEntity>

    @Query("UPDATE project_milestones SET status = :status WHERE id = :id")
    suspend fun setMilestoneStatus(id: String, status: String): Int
}
