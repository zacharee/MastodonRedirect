package dev.zwander.shared.components

import android.content.Intent
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.icerock.moko.mvvm.flow.compose.collectAsMutableState
import dev.zwander.shared.IShizukuService
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.R
import dev.zwander.shared.model.AppModel
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import dev.zwander.shared.util.Expander
import dev.zwander.shared.util.LinkSheetStatus
import dev.zwander.shared.util.LinkVerificationModel
import dev.zwander.shared.util.LinkVerifyActions.enableWithLinkSheet
import dev.zwander.shared.util.LinkVerifyActions.launchManualVerification
import dev.zwander.shared.util.Prefs
import dev.zwander.shared.util.RedirectorTheme
import dev.zwander.shared.util.ShizukuCommandResult
import dev.zwander.shared.util.ShizukuUtils.runShizukuCommand
import dev.zwander.shared.util.locals.LocalLinkSheet
import dev.zwander.shared.util.rememberLinkSheetInstallationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.ShizukuProvider
import kotlin.coroutines.CoroutineContext
import androidx.core.net.toUri

@Preview(showSystemUi = true)
@Composable
fun LinkVerifyPreview() {
    RedirectorTheme {
        Surface {
            CompositionLocalProvider(
                LocalAppModel provides object : AppModel {
                    override val launchStrategyUtils: BaseLaunchStrategyUtils
                        get() = error("Not implemented")
                    override val defaultLaunchStrategy: LaunchStrategy
                        get() = error("Not implemented")
                    override val fetchActivity: Class<*>
                        get() = error("Not implemented")
                    override val appName: String
                        get() = "Test Redirect"
                    override val prefs: Prefs
                        get() = error("Not implemented")

                    override fun postShizukuCommand(
                        context: CoroutineContext,
                        command: IShizukuService.() -> Unit
                    ) {
                    }
                }
            ) {
                LinkVerifyLayout(missingDomains = listOf("test")) {}
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkVerifyLayout(
    modifier: Modifier = Modifier,
    missingDomains: List<String>,
    refresh: () -> Unit,
) {
    val context = LocalContext.current
    val appModel = LocalAppModel.current
    val linkSheet = LocalLinkSheet.current
    val scope = rememberCoroutineScope()

    var showingShizukuInstallDialog by remember {
        mutableStateOf(false)
    }
    var showingShizukuStartDialog by remember {
        mutableStateOf(false)
    }
    var showingUnverifiedDomains by remember {
        mutableStateOf(false)
    }
    var expanded by remember {
        mutableStateOf(false)
    }
    var loading by LinkVerificationModel.isRefreshing.collectAsMutableState()

    ElevatedCard(
        modifier = modifier.padding(horizontal = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .then(if (!expanded) {
                    Modifier.clickable {
                        expanded = true
                    }
                } else {
                    Modifier
                })
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_error_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = stringResource(id = R.string.link_handling),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.headlineSmall.fontSize,
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.size(8.dp))

                Crossfade(
                    targetState = loading,
                    label = "Expander-Load-Crossfade",
                ) { ldg ->
                    if (ldg) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Expander(
                            expanded = expanded,
                            onExpand = { expanded = it },
                            modifier = Modifier.size(32.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.error,
                            ),
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                val buttonColors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer,
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.size(8.dp))

                    Text(
                        text = stringResource(
                            id = R.string.link_handling_desc,
                            appModel.appName,
                            appModel.appName,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.size(8.dp))

                    if (missingDomains.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showingUnverifiedDomains = true
                            },
                            colors = buttonColors,
                        ) {
                            Text(text = stringResource(id = R.string.unverified_domains))
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                    ) {
                        TextButton(
                            onClick = {
                                context.launchManualVerification()
                            },
                            colors = buttonColors,
                        ) {
                            Text(text = stringResource(id = R.string.settings))
                        }

                        TextButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val result = context.runShizukuCommand(Dispatchers.IO) {
                                        loading = true
                                        verifyLinks(Build.VERSION.SDK_INT, context.packageName)
                                        refresh()
                                        loading = false
                                    }

                                    showingShizukuInstallDialog =
                                        result == ShizukuCommandResult.NOT_INSTALLED
                                    showingShizukuStartDialog =
                                        result == ShizukuCommandResult.INSTALLED_NOT_RUNNING
                                }
                            },
                            enabled = !loading,
                            colors = buttonColors,
                        ) {
                            Text(text = stringResource(id = R.string.enable_using_shizuku))
                        }

                        val linkSheetStatus = rememberLinkSheetInstallationStatus()

                        TextButton(
                            onClick = {
                                context.enableWithLinkSheet(
                                    scope, linkSheet,
                                    linkSheetStatus, missingDomains,
                                    refresh,
                                )
                            },
                            colors = buttonColors,
                            enabled = !loading,
                        ) {
                            Text(
                                text = stringResource(
                                    id = when (linkSheetStatus) {
                                        LinkSheetStatus.NOT_INSTALLED -> R.string.install_linksheet
                                        LinkSheetStatus.INSTALLED_NO_INTERCONNECT -> R.string.open_linksheet
                                        LinkSheetStatus.INSTALLED_WITH_INTERCONNECT -> R.string.enable_with_linksheet
                                    },
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showingShizukuInstallDialog) {
        AlertDialog(
            onDismissRequest = { showingShizukuInstallDialog = false },
            title = { Text(text = stringResource(id = R.string.install_shizuku)) },
            text = { Text(text = stringResource(id = R.string.install_shizuku_message)) },
            dismissButton = {
                TextButton(onClick = { showingShizukuInstallDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val launchIntent =
                            Intent(Intent.ACTION_VIEW, "https://shizuku.rikka.app".toUri())
                        context.startActivity(launchIntent)
                        showingShizukuInstallDialog = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.install))
                }
            },
        )
    }

    if (showingShizukuStartDialog) {
        AlertDialog(
            onDismissRequest = { showingShizukuStartDialog = false },
            title = { Text(text = stringResource(id = R.string.start_shizuku)) },
            text = { Text(text = stringResource(id = R.string.start_shizuku_message)) },
            dismissButton = {
                TextButton(onClick = { showingShizukuStartDialog = false }) {
                    Text(text = stringResource(id = android.R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(
                            ShizukuProvider.MANAGER_APPLICATION_ID
                        )
                        context.startActivity(launchIntent)
                        showingShizukuStartDialog = false
                    }
                ) {
                    Text(text = stringResource(id = R.string.start))
                }
            },
        )
    }

    if (showingUnverifiedDomains) {
        AlertDialog(
            onDismissRequest = { showingUnverifiedDomains = false },
            title = {
                Text(
                    text = stringResource(
                        id = R.string.unverified_domains_format,
                        missingDomains.size.toString(),
                    )
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(missingDomains, { it }) {
                        Text(text = it)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showingUnverifiedDomains = false }
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            },
        )
    }
}
