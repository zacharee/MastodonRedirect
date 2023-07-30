package dev.zwander.shared

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.components.AppChooserLayout
import dev.zwander.shared.components.LinkVerifyLayout
import dev.zwander.shared.components.TextSwitch
import dev.zwander.shared.util.LinkVerifyUtils
import dev.zwander.shared.util.LocalLaunchStrategyUtils
import dev.zwander.shared.util.LocalPrefs
import dev.zwander.shared.util.RedirectorTheme
import dev.zwander.shared.util.Prefs
import dev.zwander.shared.util.rememberPreferenceState

abstract class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CompositionLocalProvider(
                LocalPrefs provides app.prefs,
                LocalLaunchStrategyUtils provides app.launchStrategyUtils,
            ) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isSystemInDarkTheme()
                    isAppearanceLightNavigationBars = isAppearanceLightStatusBars
                }

                val prefs = LocalPrefs.current

                var enableCrashReports by rememberPreferenceState(
                    key = Prefs.ENABLE_CRASH_REPORTS,
                    value = { prefs.enableCrashReports },
                ) { prefs.enableCrashReports = it }

                val (linksVerified, refresh) = LinkVerifyUtils.rememberLinkVerificationAsState()

                LaunchedEffect(key1 = enableCrashReports) {
                    if (enableCrashReports) {
                        Bugsnag.start(this@BaseActivity)
                    }
                }

                RedirectorTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .systemBarsPadding(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Column {
                                    Text(
                                        text = app.appName,
                                        style = MaterialTheme.typography.headlineLarge,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )

                                    Text(
                                        text = app.versionName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )
                                }

                                AnimatedVisibility(visible = !linksVerified.value) {
                                    LinkVerifyLayout(
                                        refresh = refresh,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    AppChooserLayout(
                                        modifier = Modifier.fillMaxWidth(),
                                    )
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
                            }
                        }
                    }
                }
            }
        }
    }
}