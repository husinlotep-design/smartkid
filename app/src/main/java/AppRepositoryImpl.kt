package com.smartkids.launcher.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Base64
import com.smartkids.launcher.data.local.AppDao
import com.smartkids.launcher.data.local.AppEntity
import com.smartkids.launcher.domain.model.AppModel
import com.smartkids.launcher.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao
) : AppRepository {

    override suspend fun syncInstalledApps() = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = context.packageManager.queryIntentActivities(intent, 0)

        val entities = resolvedApps.map { info ->
            AppEntity(
                packageName = info.activityInfo.packageName,
                appName = info.loadLabel(context.packageManager).toString(),
                iconBase64 = drawableToBase64(info.loadIcon(context.packageManager))
            )
        }

        entities.forEach { appDao.insertIfNotExists(it) }
        val installedPackages = entities.map { it.packageName }
        appDao.removeUninstalledApps(installedPackages)
    }

    override fun observeApprovedApps(): Flow<List<AppModel>> =
        appDao.observeApprovedApps().map { list -> list.map(::entityToDomain) }

    override fun observeAllApps(): Flow<List<AppModel>> =
        appDao.observeAllApps().map { list -> list.map(::entityToDomain) }

    override suspend fun setApproval(packageName: String, approved: Boolean) =
        withContext(Dispatchers.IO) { appDao.setApproval(packageName, approved) }

    override suspend fun setDailyLimit(packageName: String, minutes: Int) =
        withContext(Dispatchers.IO) { appDao.setDailyLimit(packageName, minutes) }

    private fun entityToDomain(e: AppEntity) = AppModel(
        packageName = e.packageName,
        appName = e.appName,
        isApproved = e.isApproved,
        dailyLimitMinutes = e.dailyLimitMinutes,
        iconBase64 = e.iconBase64
    )

    private fun drawableToBase64(drawable: Drawable): String {
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        Canvas(bitmap).also {
            drawable.setBounds(0, 0, it.width, it.height)
            drawable.draw(it)
        }
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, out)
            Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
        }
    }
}