package com.smartkids.launcher.domain.repository

import com.smartkids.launcher.domain.model.AppModel
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    suspend fun syncInstalledApps()
    fun observeApprovedApps(): Flow<List<AppModel>>
    fun observeAllApps(): Flow<List<AppModel>>
    suspend fun setApproval(packageName: String, approved: Boolean)
    suspend fun setDailyLimit(packageName: String, minutes: Int)
}