package com.smartkids.launcher.data.local

import androidx.room.*

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isApproved: Boolean = false,
    val dailyLimitMinutes: Int = 0,
    val iconBase64: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)