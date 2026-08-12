package com.example.prepx.data.db

import androidx.room.*
import com.example.prepx.data.model.PlannerItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room database operations on planner items.
 */
@Dao
interface PlannerDao {

    @Query("SELECT * FROM planner_items WHERE userId = :userId OR userId = '' ORDER BY dateTime ASC")
    fun getAllItemsSortedByDate(userId: String): Flow<List<PlannerItem>>

    @Query("SELECT * FROM planner_items WHERE isCompleted = 1 AND (userId = :userId OR userId = '') ORDER BY dateTime DESC")
    fun getCompletedGoals(userId: String): Flow<List<PlannerItem>>

    @Query("SELECT * FROM planner_items WHERE id = :id")
    suspend fun getItemById(id: Long): PlannerItem?

    @Query("SELECT * FROM planner_items WHERE externalId = :externalId AND (userId = :userId OR userId = '') LIMIT 1")
    suspend fun getItemByExternalId(externalId: String, userId: String): PlannerItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PlannerItem): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItems(items: List<PlannerItem>): List<Long>

    @Update
    suspend fun updateItem(item: PlannerItem)

    @Delete
    suspend fun deleteItem(item: PlannerItem)

    @Query("UPDATE planner_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("SELECT * FROM planner_items WHERE reminderEnabled = 1")
    suspend fun getActiveReminderItems(): List<PlannerItem>

    @Query("DELETE FROM planner_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)

    @Query("DELETE FROM planner_items")
    suspend fun deleteAllItems()
}
