package dev.zwander.mastodonredirect.shizuku

import android.os.Build
import dev.zwander.mastodonredirect.IShizukuService
import dev.zwander.mastodonredirect.util.LinkVerifyUtils

class ShizukuService : IShizukuService.Stub() {
    override fun verifyLinks(packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Runtime.getRuntime().exec("cmd package set-app-links --package $packageName 2 all")
                .waitFor()
        }
        LinkVerifyUtils.verifyAllLinks(packageName)
    }

    override fun destroy() {}
}