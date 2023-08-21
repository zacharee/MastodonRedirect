package dev.zwander.shared.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openLinkInBrowser(uri: Uri?) {
    val launchIntent = Intent(Intent.ACTION_VIEW, uri)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    launchIntent.addCategory(Intent.CATEGORY_DEFAULT)
    launchIntent.addCategory(Intent.CATEGORY_BROWSABLE)
    launchIntent.selector = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))
        .addCategory(Intent.CATEGORY_BROWSABLE)

    try {
        startActivity(launchIntent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}

fun Context.openLinkNaturally(uri: Uri?) {
    val launchIntent = Intent(Intent.ACTION_VIEW, uri)
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    launchIntent.addCategory(Intent.CATEGORY_DEFAULT)
    launchIntent.addCategory(Intent.CATEGORY_BROWSABLE)

    try {
        startActivity(launchIntent)
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }
}
