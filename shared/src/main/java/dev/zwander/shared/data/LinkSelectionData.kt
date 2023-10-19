package dev.zwander.shared.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import dev.zwander.shared.util.prefs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map

data class LinkSelectionData(
    val host: String,
): Comparable<LinkSelectionData> {
    override fun compareTo(other: LinkSelectionData): Int {
        return host.compareTo(other.host)
    }

    @Composable
    fun updateLinkBlockedStatusAsState(): State<Boolean> {
        val context = LocalContext.current

        return context.prefs.blocklistedDomains.value.map { it?.contains(host) == true }.collectAsState(
            initial = false
        )
    }

    suspend fun Context.updateLinkBlockedStatus(selected: Boolean) = coroutineScope {
        val current = prefs.blocklistedDomains.currentValue(this).toMutableSet()

        if (selected) {
            current.add(host)
        } else {
            current.remove(host)
        }

        prefs.blocklistedDomains.set(current)
    }
}
