package dev.zwander.shared.shizuku

import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import dev.zwander.shared.IShizukuService
import dev.zwander.shared.data.VerifyResult
import dev.zwander.shared.util.LinkVerifyUtils
import kotlin.system.exitProcess

class ShizukuService : IShizukuService.Stub {
    @Keep
    constructor() : super()
    @Keep
    constructor(@Suppress("UNUSED_PARAMETER") context: Context) : super()

    override fun verifyLinks(sdk: Int, packageName: String): VerifyResult? {
        try {
            return if (sdk >= Build.VERSION_CODES.S) {
                val output = ArrayList<String>()

                val process = Runtime.getRuntime().exec("cmd package set-app-links --package $packageName 2 all")
                process.inputStream.bufferedReader().forEachLine { output.add(it) }
                process.errorStream.bufferedReader().forEachLine { output.add(it) }

                val result = process.waitFor()

                VerifyResult(
                    output = output,
                    result = result,
                )
            } else {
                null
            }
        } finally {
            LinkVerifyUtils.verifyAllLinks(packageName)
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}