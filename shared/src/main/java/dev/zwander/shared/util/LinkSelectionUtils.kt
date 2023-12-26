package dev.zwander.shared.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.shared.data.LinkSelectionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dongliu.apk.parser.ApkFile
import java.util.TreeSet

private fun List<LinkSelectionData>.sortByStatus(blocklist: Set<String>): List<LinkSelectionData> {
    return sortedWith { o1, o2 ->
        if (blocklist.contains(o1.host) && !blocklist.contains(o2.host)) {
            -1
        } else if (!blocklist.contains(o1.host) && blocklist.contains(o2.host)) {
            1
        } else {
            o1.compareTo(o2)
        }
    }
}

@Composable
fun collectAvailableDomainsSortedByStatus(): State<List<LinkSelectionData>> {
    val context = LocalContext.current

    val baseSet = remember {
        mutableStateOf<List<LinkSelectionData>>(listOf())
    }

    val blocklistedDomains by context.prefs.blocklistedDomains.value.collectAsState(initial = setOf())

    LaunchedEffect(key1 = null) {
        baseSet.value = withContext(Dispatchers.IO) {
            context.collectAvailableDomains().sortByStatus(blocklistedDomains ?: setOf())
        }
    }

    LaunchedEffect(key1 = blocklistedDomains, key2 = baseSet.value.isEmpty()) {
        baseSet.value = withContext(Dispatchers.IO) {
            baseSet.value.sortByStatus(blocklistedDomains ?: setOf())
        }
    }

    return baseSet
}

fun Context.collectAvailableDomains(): List<LinkSelectionData> {
    val apk = ApkFile(applicationInfo.sourceDir)
    val hosts = TreeSet<LinkSelectionData>()

    val regex = Regex(".*?android:host=\"(.*?)\".*?")

    apk.manifestXml.reader().forEachLine {
        regex.matchEntire(it)?.groups?.get(1)?.let { group ->
            hosts.add(LinkSelectionData(group.value))
        }
    }

    return hosts.toList()
}
