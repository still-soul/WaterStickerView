package com.ztk.waterstickerview.utils

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * app相关的工具类
 * @author xiaoman
 */
object AppUtil {
    /**
     * 判断是否debug版本
     */
    fun isDebugVersion(context: Context): Boolean {
        try {
            val pkgName = context.packageName
            val packageInfo = context.packageManager.getPackageInfo(
                pkgName, 0
            )
            if (packageInfo != null) {
                val info = packageInfo.applicationInfo
                return info.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}