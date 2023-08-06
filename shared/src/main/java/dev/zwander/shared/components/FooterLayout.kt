package dev.zwander.shared.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.Prefs
import dev.zwander.shared.util.openLinkInBrowser
import dev.zwander.shared.util.rememberPreferenceState
import tk.zwander.patreonsupportersretrieval.view.SupporterView

private data class FooterButton(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FooterLayout(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    var showingSettingsDialog by remember {
        mutableStateOf(false)
    }
    var showingSupportersDialog by remember {
        mutableStateOf(false)
    }

    val buttons = remember {
        listOf(
            FooterButton(
                R.string.github,
                R.drawable.github,
            ) {
                context.openLinkInBrowser(Uri.parse("https://github.com/zacharee/MastodonRedirect/"))
            },
            FooterButton(
                R.string.patreon,
                R.drawable.patreon,
            ) {
                context.openLinkInBrowser(Uri.parse("https://www.patreon.com/zacharywander"))
            },
            FooterButton(
                R.string.supporters,
                R.drawable.heart,
            ) {
                showingSupportersDialog = true
            },
            FooterButton(
                R.string.options,
                R.drawable.baseline_settings_24,
            ) {
                showingSettingsDialog = true
            },
        )
    }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        buttons.forEach { button ->
            IconButton(
                onClick = button.onClick,
            ) {
                Icon(
                    painter = painterResource(id = button.iconRes),
                    contentDescription = stringResource(id = button.labelRes),
                )
            }
        }
    }

    if (showingSettingsDialog) {
        OptionsDialog {
            showingSettingsDialog = false
        }
    }

    if (showingSupportersDialog) {
        SupportersDialog {
            showingSupportersDialog = false
        }
    }
}

@Composable
private fun SupportersDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.supporters))
        },
        text = {
            AndroidView(
                factory = { SupporterView(it) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
    )
}

@Composable
private fun OptionsDialog(
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = LocalAppModel.current.prefs

    var enableCrashReports by rememberPreferenceState(
        key = Prefs.ENABLE_CRASH_REPORTS,
        value = { prefs.enableCrashReports },
    ) { prefs.enableCrashReports = it }

    LaunchedEffect(key1 = enableCrashReports) {
        if (enableCrashReports) {
            Bugsnag.start(context)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.options))
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    TextSwitch(
                        text = stringResource(id = R.string.enable_crash_reports),
                        subtitle = stringResource(id = R.string.enable_crash_reports_desc),
                        checked = enableCrashReports,
                        onCheckedChange = { enableCrashReports = it },
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        },
    )
}