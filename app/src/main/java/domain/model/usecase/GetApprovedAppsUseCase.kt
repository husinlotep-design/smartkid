package com.smartkids.launcher.domain.usecase

import com.smartkids.launcher.domain.model.AppModel
import com.smartkids.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApprovedAppsUseCase @Inject constructor(
    private val repository: AppRepository
) {
    operator fun invoke(): Flow<List<AppModel>> = repository.observeApprovedApps()
}