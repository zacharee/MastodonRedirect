package dev.zwander.mastodonredirect.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.mastodonredirect.R

val launchStrategies = mapOf(
    Megalodon.key to Megalodon,
    Fedilab.key to Fedilab,
    SubwayTooter.key to SubwayTooter,
    Moshidon.key to Moshidon,
)

@Composable
fun rememberSortedLaunchStrategies(): List<LaunchStrategy> {
    val context = LocalContext.current

    return remember {
        launchStrategies.values.sortedBy {
            context.resources.getString(it.labelRes).lowercase()
        }
    }
}

sealed class LaunchStrategy(
    val key: String,
    @StringRes val labelRes: Int,
) {
    abstract fun Context.createIntents(url: String?): List<Intent>
}

object Megalodon : LaunchStrategy("MEGALODON", R.string.megalodon) {
    override fun Context.createIntents(url: String?): List<Intent> {
        return listOf(
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, url)
                `package` = "org.joinmastodon.android.sk"
                component = ComponentName(
                    "org.joinmastodon.android.sk",
                    "org.joinmastodon.android.ExternalShareActivity"
                )
            },
        )
    }
}

object Fedilab : LaunchStrategy("FEDILAB", R.string.fedilab) {
    override fun Context.createIntents(url: String?): List<Intent> {
        val baseIntent = Intent(Intent.ACTION_VIEW)
        baseIntent.data = url?.let { Uri.parse(url) }

        return listOf(
            Intent(baseIntent).apply {
                `package` = "app.fedilab.android"
                component = ComponentName(
                    "app.fedilab.android",
                    "app.fedilab.android.activities.MainActivity"
                )

            },
            Intent(baseIntent).apply {
                `package` = "fr.gouv.etalab.mastodon"
                component = ComponentName(
                    "fr.gouv.etalab.mastodon",
                    "app.fedilab.android.activities.MainActivity"
                )
            }
        )
    }
}

object SubwayTooter : LaunchStrategy("SUBWAY_TOOTER", R.string.subway_tooter) {
    override fun Context.createIntents(url: String?): List<Intent> {
        return listOf(
            Intent(Intent.ACTION_VIEW).apply {
                `package` = "jp.juggler.subwaytooter"
                component = ComponentName(
                    "jp.juggler.subwaytooter",
                    "jp.juggler.subwaytooter.ActCallback"
                )
                data = url?.let { Uri.parse(url) }
            },
        )
    }
}

object Moshidon : LaunchStrategy("MOSHIDON", R.string.moshidon) {
    override fun Context.createIntents(url: String?): List<Intent> {
        return listOf(
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, url)
                `package` = "org.joinmastodon.android.moshinda"
                component = ComponentName(
                    "org.joinmastodon.android.moshinda",
                    "org.joinmastodon.android.ExternalShareActivity"
                )
            },
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, url)
                `package` = "org.joinmastodon.android.moshinda.nightly"
                component = ComponentName(
                    "org.joinmastodon.android.moshinda.nightly",
                    "org.joinmastodon.android.ExternalShareActivity"
                )
            },
        )
    }
}
