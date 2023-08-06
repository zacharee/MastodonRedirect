package dev.zwander.shared.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerifyUtils.launchManualVerification
import dev.zwander.shared.util.ShizukuPermissionUtils.isShizukuInstalled
import dev.zwander.shared.util.ShizukuPermissionUtils.isShizukuRunning
import dev.zwander.shared.util.ShizukuPermissionUtils.rememberHasPermissionAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkVerifyLayout(
    modifier: Modifier = Modifier,
    missingDomains: List<String>,
    refresh: () -> Unit,
) {
    val context = LocalContext.current
    val appModel = LocalAppModel.current
    val scope = rememberCoroutineScope()

    val shizukuPermission by rememberHasPermissionAsState()

    var showingShizukuInstallDialog by remember {
        mutableStateOf(false)
    }
    var showingShizukuStartDialog by remember {
        mutableStateOf(false)
    }
    var showingUnverifiedDomains by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.link_handling),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Text(
            text = stringResource(
                id = R.string.link_handling_desc,
                appModel.appName,
                appModel.appName,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.size(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    context.launchManualVerification()
                }
            ) {
                Text(text = stringResource(id = R.string.enable))
            }

            Button(
                onClick = {
                    if (isShizukuRunning) {
                        if (shizukuPermission) {
                            appModel.postShizukuCommand {
                                verifyLinks(Build.VERSION.SDK_INT, context.packageName)
                                refresh()
                            }
                        } else {
                            scope.launch(Dispatchers.IO) {
                                val granted = suspendCoroutine { cont ->
                                    val listener = object : Shizuku.OnRequestPermissionResultListener {
                                        override fun onRequestPermissionResult(
                                            requestCode: Int,
                                            grantResult: Int
                                        ) {
                                            Shizuku.removeRequestPermissionResultListener(this)
                                            cont.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                                        }
                                    }
                                    Shizuku.addRequestPermissionResultListener(listener)
                                    Shizuku.requestPermission(100)
                                }

                                if (granted) {
                                    appModel.postShizukuCommand {
                                        verifyLinks(Build.VERSION.SDK_INT, context.packageName)
                                        refresh()
                                    }
                                }
                            }
                        }
                    } else {
                        val installed = context.isShizukuInstalled

                        showingShizukuInstallDialog = !installed
                        showingShizukuStartDialog = installed
                    }
                },
            ) {
                Text(text = stringResource(id = R.string.enable_using_shizuku))
            }

            Button(
                onClick = {
                    val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/1fexd/LinkSheet"))
                    launchIntent.addCategory(Intent.CATEGORY_DEFAULT)
                    launchIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                    launchIntent.selector = Intent(Intent.ACTION_VIEW, Uri.parse("https://"))

                    try {
                        context.startActivity(launchIntent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                },
            ) {
                Text(text = stringResource(id = R.string.install_linksheet))
            }

            if (missingDomains.isNotEmpty()) {
                Button(
                    onClick = {
                        showingUnverifiedDomains = true
                    },
                ) {
                    Text(text = stringResource(id = R.string.unverified_domains))
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
                        val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app"))
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
                            ShizukuProvider.MANAGER_APPLICATION_ID)
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
            title = { Text(text = stringResource(id = R.string.unverified_domains)) },
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
