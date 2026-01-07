package dev.zwander.shared.util.locals

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import app.linksheet.lib.flavors.LinkSheet
import app.linksheet.lib.interconnect.LinkSheetConnector
import dev.zwander.shared.util.LifecycleEffect

val LocalLinkSheetConnector = compositionLocalOf<LinkSheetConnector?> { null }

private fun LinkSheet.getInterconnect(context: Context): LinkSheetConnector? {
    return findInterconnect(context).firstOrNull()?.let { LinkSheetConnector(it) }
}

@Composable
fun rememberLinkSheetConnector(): State<LinkSheetConnector?> {
    val context = LocalContext.current
    val linkSheet = remember { LinkSheet() }

    val interconnect = remember {
        mutableStateOf(linkSheet.getInterconnect(context))
    }

    LifecycleEffect(triggerOn = arrayOf(Lifecycle.State.RESUMED)) {
        interconnect.value = linkSheet.getInterconnect(context)
    }

    return interconnect
}
