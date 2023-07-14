package dev.zwander.mastodonredirect

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.view.WindowCompat
import com.bugsnag.android.Bugsnag
import dev.zwander.mastodonredirect.components.TextSwitch
import dev.zwander.mastodonredirect.ui.theme.MastodonRedirectTheme
import dev.zwander.mastodonredirect.util.LinkVerifyUtils.launchManualVerification
import dev.zwander.mastodonredirect.util.LinkVerifyUtils.rememberLinkVerificationAsState
import dev.zwander.mastodonredirect.util.Prefs
import dev.zwander.mastodonredirect.util.ShizukuPermissionUtils.isShizukuInstalled
import dev.zwander.mastodonredirect.util.ShizukuPermissionUtils.isShizukuRunning
import dev.zwander.mastodonredirect.util.ShizukuPermissionUtils.rememberHasPermissionAsState
import dev.zwander.mastodonredirect.util.launchStrategies
import dev.zwander.mastodonredirect.util.prefs
import dev.zwander.mastodonredirect.util.rememberPreferenceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    @SuppressLint("WrongConstant")
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isSystemInDarkTheme()
                isAppearanceLightNavigationBars = isAppearanceLightStatusBars
            }

            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            var selectedStrategy by context.rememberPreferenceState(
                key = Prefs.SELECTED_APP,
                value = { prefs.selectedApp },
                onChanged = { prefs.selectedApp = it },
            )
            var enableCrashReports by context.rememberPreferenceState(
                key = Prefs.ENABLE_CRASH_REPORTS,
                value = { prefs.enableCrashReports },
                onChanged = { prefs.enableCrashReports = it },
            )

            val (linksVerified, refresh) = rememberLinkVerificationAsState()
            val shizukuPermission by rememberHasPermissionAsState()

            var showingShizukuInstallDialog by remember {
                mutableStateOf(false)
            }
            var showingShizukuStartDialog by remember {
                mutableStateOf(false)
            }

            LaunchedEffect(key1 = enableCrashReports) {
                if (enableCrashReports) {
                    Bugsnag.start(this@MainActivity)
                }
            }

            MastodonRedirectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .systemBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )

                        AnimatedVisibility(visible = !linksVerified.value) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.link_handling),
                                    style = MaterialTheme.typography.headlineSmall,
                                    textAlign = TextAlign.Center,
                                )

                                Text(
                                    text = stringResource(id = R.string.link_handling_desc),
                                    textAlign = TextAlign.Center,
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
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
                                                    (context.applicationContext as App).postShizukuCommand { verifyLinks(packageName) }
                                                    refresh()
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
                                                            (context.applicationContext as App).postShizukuCommand { verifyLinks(packageName) }
                                                            refresh()
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
                                }
                            }
                        }

                        TextSwitch(
                            text = stringResource(id = R.string.enable_crash_reports),
                            subtitle = stringResource(id = R.string.enable_crash_reports_desc),
                            checked = enableCrashReports,
                            onCheckedChange = { enableCrashReports = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = stringResource(id = R.string.choose_app),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                            )

                            Text(
                                text = stringResource(id = R.string.choose_app_desc),
                                textAlign = TextAlign.Center,
                            )
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(launchStrategies.entries.toList(), { it.key }) { (key, strategy) ->
                                val color by animateColorAsState(
                                    targetValue = if (selectedStrategy == strategy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    label = "CardColor-$key",
                                )

                                ElevatedCard(
                                    onClick = { selectedStrategy = strategy },
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = color,
                                    ),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = stringResource(id = strategy.labelRes),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            color = if (selectedStrategy == strategy) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSecondary
                                            },
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
                                    val launchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://shizuku.rikka.app"))
                                    startActivity(launchIntent)
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
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(ShizukuProvider.MANAGER_APPLICATION_ID)
                                    startActivity(launchIntent)
                                }
                            ) {
                                Text(text = stringResource(id = R.string.start))
                            }
                        },
                    )
                }
            }
        }
    }
}
