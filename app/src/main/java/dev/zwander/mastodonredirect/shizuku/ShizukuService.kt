package dev.zwander.mastodonredirect.shizuku

import dev.zwander.mastodonredirect.IShizukuService

class ShizukuService : IShizukuService.Stub() {
    override fun verifyLinks(packageName: String?) {
        Runtime.getRuntime().exec("cmd package set-app-links --package $packageName 2 all")
            .waitFor()
    }

    override fun destroy() {}
}