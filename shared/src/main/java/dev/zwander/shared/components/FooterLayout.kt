package dev.zwander.shared.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.LinkBlocklistActivity
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerificationModel
import dev.zwander.shared.util.ShizukuUtils.runShizukuCommand
import dev.zwander.shared.util.openLinkNaturally
import dev.zwander.shared.util.rememberMutablePreferenceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tk.zwander.patreonsupportersretrieval.view.SupporterView

private data class FooterButton(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int,
    val onLongClick: (() -> Unit)? = null,
    val onClick: () -> Unit,
)

@OptIn(
    ExperimentalLayoutApi::class,
)
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
    var showingShizukuResetDialog by remember {
        mutableStateOf(false)
    }

    val buttons = remember {
        listOf(
            FooterButton(
                R.string.github,
                R.drawable.github,
            ) {
                context.openLinkNaturally(Uri.parse("https://github.com/zacharee/MastodonRedirect/"))
            },
            FooterButton(
                R.string.patreon,
                R.drawable.patreon,
            ) {
                context.openLinkNaturally(Uri.parse("https://www.patreon.com/zacharywander"))
            },
            FooterButton(
                R.string.mastodon,
                R.drawable.mastodon,
            ) {
                context.openLinkNaturally(Uri.parse("https://androiddev.social/@wander1236"))
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
                onLongClick = {
                    showingShizukuResetDialog = true
                },
            ) {
                showingSettingsDialog = true
            },
        )
    }

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        FlowRow(
            modifier = modifier.padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            buttons.forEach { button ->
                LongClickableIconButton(
                    onClick = button.onClick,
                    onLongClick = button.onLongClick,
                ) {
                    Icon(
                        painter = painterResource(id = button.iconRes),
                        contentDescription = stringResource(id = button.labelRes),
                    )
                }
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

    if (showingShizukuResetDialog) {
        ShizukuResetDialog {
            showingShizukuResetDialog = false
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
    val appModel = LocalAppModel.current
    val prefs = appModel.prefs

    var enableCrashReports by prefs.enableCrashReports.rememberMutablePreferenceState()
    var openMediaInBrowser by prefs.openMediaInBrowser.rememberMutablePreferenceState()

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
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    TextSwitch(
                        text = stringResource(id = R.string.open_media_in_browser),
                        subtitle = stringResource(
                            id = R.string.open_media_in_browser_desc,
                            appModel.appName
                        ),
                        checked = openMediaInBrowser,
                        onCheckedChange = { openMediaInBrowser = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    AnimatedCard(
                        onClick = {
                            context.startActivity(Intent(context, LinkBlocklistActivity::class.java))
                        },
                        colors = CardDefaults.outlinedCardColors(),
                        border = null,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = stringResource(id = R.string.link_blocklist),
                                )

                                Text(
                                    text = stringResource(id = R.string.link_blocklist_desc, appModel.appName),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
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

@Composable
private fun ShizukuResetDialog(
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.reset_link_verification))
        },
        text = {
            Text(text = stringResource(id = R.string.reset_link_verification_desc))
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.no))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        context.runShizukuCommand(Dispatchers.IO) {
                            unverifyLinks(Build.VERSION.SDK_INT, context.packageName)
                            LinkVerificationModel.refresh()
                        }
                    }
                    onDismissRequest()
                },
            ) {
                Text(
                    text = stringResource(id = R.string.yes),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}
