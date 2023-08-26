package dev.zwander.shared.util.locals

import androidx.compose.runtime.compositionLocalOf
import fe.linksheet.interconnect.LinkSheet

val LocalLinkSheet = compositionLocalOf<LinkSheet?> { null }
