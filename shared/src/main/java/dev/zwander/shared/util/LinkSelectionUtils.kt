package dev.zwander.shared.util

import android.content.Context
import dev.zwander.shared.data.LinkSelectionData
import net.dongliu.apk.parser.ApkFile
import java.util.TreeSet

fun Context.collectAvailableDomains(): List<LinkSelectionData> {
    val apk = ApkFile(applicationInfo.baseCodePath)
    val hosts = TreeSet<LinkSelectionData>()

    val regex = Regex(".*?android:host=\"(.*?)\".*?")

    apk.manifestXml.reader().forEachLine {
        regex.matchEntire(it)?.groups?.get(1)?.let { group ->
            hosts.add(LinkSelectionData(group.value))
        }
    }

    return hosts.toList()
}
