package dev.zwander.shared.shizuku

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.UserHandle
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

    @SuppressLint("PrivateApi")
    override fun verifyLinks(sdk: Int, packageName: String): List<VerifyResult> {
        try {
            return if (sdk >= Build.VERSION_CODES.S) {
                val setOutput = ArrayList<String>()

                // Use set-app-links-user-selection instead of
                // set-app-links to avoid having to use `autoVerify="true"`
                // in manifest, which can cause excessive battery drain
                // if something like LinkSheet is used instead of verifying
                // links with Shizuku.
                // Also, this needs to be a command, since there's a special
                // internal version of the API only accessible to the shell
                // command that bypasses normal permission checks.
                // DomainManager#setDomainVerificationUserSelection requires
                // UPDATE_DOMAIN_VERIFICATION_USER_SELECTION, which the shell
                // user doesn't hold.
                val setResult = runCommand(
                    "cmd package set-app-links-user-selection" +
                            " --user ${UserHandle.USER_ALL}" +
                            " --package $packageName true all",
                    setOutput,
                )

                listOf(
                    VerifyResult(setOutput, setResult),
                )
            } else {
                listOf()
            }
        } finally {
            LinkVerifyUtils.verifyAllLinks(packageName)
        }
    }

    override fun unverifyLinks(sdk: Int, packageName: String): List<VerifyResult> {
        try {
            return if (sdk >= Build.VERSION_CODES.S) {
                val setOutput = ArrayList<String>()

                val setResult = runCommand(
                    "cmd package set-app-links-user-selection" +
                            " --user ${UserHandle.USER_ALL}" +
                            " --package $packageName false all",
                    setOutput,
                )

                listOf(
                    VerifyResult(setOutput, setResult),
                )
            } else {
                listOf()
            }
        } finally {
            LinkVerifyUtils.unverifyAllLinks(packageName)
        }
    }

    override fun destroy() {
        exitProcess(0)
    }

    private fun runCommand(command: String, output: MutableList<String>): Int {
        val process = Runtime.getRuntime().exec(command)
        process.inputStream.bufferedReader().forEachLine { output.add(it) }
        process.errorStream.bufferedReader().forEachLine { output.add(it) }

        return process.waitFor()
    }
}