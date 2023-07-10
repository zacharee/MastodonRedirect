package dev.zwander.mastodonredirect

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import dev.zwander.mastodonredirect.ui.theme.MastodonRedirectTheme
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MainActivity : ComponentActivity() {
    @SuppressLint("InlinedApi", "WrongConstant")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val domain = getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as DomainVerificationManager

        setContent {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isSystemInDarkTheme()
                isAppearanceLightNavigationBars = isAppearanceLightStatusBars
            }

            MastodonRedirectTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val context = LocalContext.current
                    var selectedStrategy by context.rememberPreferenceState(
                        key = Prefs.SELECTED_APP,
                        value = { prefs.selectedApp },
                        onChanged = { prefs.selectedApp = it },
                    )

                    var showDomainStateAlert by remember {
                        mutableStateOf(false)
                    }

                    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()

                    LaunchedEffect(key1 = lifecycleState) {
                        if (lifecycleState == Lifecycle.State.RESUMED) {
                            showDomainStateAlert = domain.getDomainVerificationUserState(packageName)?.hostToStateMap?.any { (_, state) ->
                                state == DomainVerificationUserState.DOMAIN_STATE_NONE
                            } == true
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize()
                            .imePadding()
                            .systemBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AnimatedVisibility(visible = showDomainStateAlert) {
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

                                Button(
                                    onClick = {
                                        val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                            Intent("android.settings.APPLICATION_DETAILS_SETTINGS_OPEN_BY_DEFAULT_PAGE").apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        } else {
                                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        }

                                        startActivity(settingsIntent)
                                    }
                                ) {
                                    Text(text = stringResource(id = R.string.enable))
                                }
                            }
                        }

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
            }
        }
    }
}
