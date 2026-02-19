package com.thanksplay.adesk.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val className: String,
    val label: String,
    val icon: Drawable,
    val installTime: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppInfo) return false
        return packageName == other.packageName && className == other.className
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + className.hashCode()
        return result
    }
}
