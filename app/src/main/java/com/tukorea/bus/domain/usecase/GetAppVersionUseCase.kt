package com.tukorea.bus.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetAppVersionUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    operator fun invoke(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }
}

