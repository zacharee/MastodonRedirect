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

    override fun verifyLinks(sdk: Int, packageName: String): List<VerifyResult> {
        try {
            return if (sdk >= Build.VERSION_CODES.S) {
                val resetOutput = ArrayList<String>()
                val setOutput = ArrayList<String>()

                val resetResult = runCommand("cmd package reset-app-links $packageName", resetOutput)
                val setResult = runCommand("cmd package set-app-links --package $packageName 2 all", setOutput)

                listOf(
                    VerifyResult(resetOutput, resetResult),
                    VerifyResult(setOutput, setResult),
                )
            } else {
                listOf()
            }
        } finally {
            LinkVerifyUtils.verifyAllLinks(packageName)
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