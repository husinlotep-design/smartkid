package com.smartkids.launcher.domain.usecase

import com.smartkids.launcher.domain.repository.AppRepository
import javax.inject.Inject

class UpdateApprovedAppUseCase @Inject constructor(
    private val repository: AppRepository
) {
    suspend operator fun invoke(packageName: String, isApproved: Boolean) {
        repository.setApproval(packageName, isApproved)
    }
}