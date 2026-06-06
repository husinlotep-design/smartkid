package com.smartkids.launcher.domain.usecase

import com.smartkids.launcher.domain.repository.AppRepository
import javax.inject.Inject

class SetAppTimeLimitUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(packageName: String, minutes: Int) {
        repository.setDailyLimit(packageName, minutes)
    }
}