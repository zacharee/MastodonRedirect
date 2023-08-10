package dev.zwander.shared.util.hiddenapi

import android.annotation.SuppressLint
import dev.zwander.shared.util.hiddenapi.HiddenApiUtils.asInterface
import dev.zwander.shared.util.hiddenapi.HiddenApiUtils.currentUserId
import rikka.shizuku.SystemServiceHelper

@SuppressLint("PrivateApi")
object PackageManager {
    object VerificationStatus {
        const val ALWAYS = 2
        const val ALWAYS_ASK = 4
    }

    private val iPmClass = Class.forName("android.content.pm.IPackageManager")
    private val iPmStubClass = Class.forName("android.content.pm.IPackageManager\$Stub")

    fun setLinkVerificationState(packageName: String, status: Int): Boolean {
        val pmInstance = iPmStubClass.asInterface(SystemServiceHelper.getSystemService("package"))

        return iPmClass.getMethod(
            "updateIntentVerificationStatus",
            String::class.java,
            Int::class.java,
            Int::class.java,
        ).invoke(
            pmInstance,
            packageName,
            status,
            currentUserId,
        ) as Boolean
    }

    fun getIntentVerificationStatus(packageName: String): Int {
        val pmInstance = iPmStubClass.asInterface(SystemServiceHelper.getSystemService("package"))

        return iPmClass.getMethod(
            "getIntentVerificationStatus",
            String::class.java,
            Int::class.java,
        ).invoke(
            pmInstance,
            packageName,
            currentUserId,
        ) as Int
    }
}
