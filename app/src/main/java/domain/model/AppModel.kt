package com.smartkids.launcher.domain.model

data class AppModel(
    val packageName: String,
    val appName: String,
    val isApproved: Boolean,
    val dailyLimitMinutes: Int,
    val iconBase64: String?
)