package com.smartkids.launcher.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps WHERE isApproved = 1 ORDER BY appName ASC")
    fun observeApprovedApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps ORDER BY appName ASC")
    fun observeAllApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(app: AppEntity)

    @Update
    suspend fun updateApp(app: AppEntity)

    @Query("UPDATE apps SET isApproved = :approved WHERE packageName = :packageName")
    suspend fun setApproval(packageName: String, approved: Boolean)

    @Query("UPDATE apps SET dailyLimitMinutes = :minutes WHERE packageName = :packageName")
    suspend fun setDailyLimit(packageName: String, minutes: Int)

    @Query("DELETE FROM apps WHERE packageName NOT IN (:installedPackages)")
    suspend fun removeUninstalledApps(installedPackages: List<String>)
}