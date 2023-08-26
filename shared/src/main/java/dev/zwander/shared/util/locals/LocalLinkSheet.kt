package dev.zwander.shared.util.locals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import dev.zwander.shared.util.LifecycleEffect
import fe.linksheet.interconnect.LinkSheet
import fe.linksheet.interconnect.LinkSheetConnector

val LocalLinkSheet = compositionLocalOf<LinkSheet?> { null }

@Composable
fun rememberLinkSheet(): State<LinkSheet?> {
    val context = LocalContext.current

    val linkSheet = remember {
        mutableStateOf(LinkSheetConnector.getLinkSheet(context))
    }

    LifecycleEffect(triggerOn = arrayOf(Lifecycle.State.RESUMED)) {
        linkSheet.value = LinkSheetConnector.getLinkSheet(context)
    }

    return linkSheet
}
