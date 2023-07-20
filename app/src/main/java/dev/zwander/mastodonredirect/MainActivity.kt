package dev.zwander.mastodonredirect

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.bugsnag.android.Bugsnag
import dev.zwander.mastodonredirect.components.AppChooserLayout
import dev.zwander.mastodonredirect.components.LinkVerifyLayout
import dev.zwander.mastodonredirect.components.TextSwitch
import dev.zwander.mastodonredirect.ui.theme.MastodonRedirectTheme
import dev.zwander.mastodonredirect.util.LinkVerifyUtils.rememberLinkVerificationAsState
import dev.zwander.mastodonredirect.util.Prefs
import dev.zwander.mastodonredirect.util.prefs
import dev.zwander.mastodonredirect.util.rememberPreferenceState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isSystemInDarkTheme()
                isAppearanceLightNavigationBars = isAppearanceLightStatusBars
            }

            val context = LocalContext.current

            var enableCrashReports by context.rememberPreferenceState(
                key = Prefs.ENABLE_CRASH_REPORTS,
                value = { prefs.enableCrashReports },
                onChanged = { prefs.enableCrashReports = it },
            )

            val (linksVerified, refresh) = rememberLinkVerificationAsState()

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
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                )

                                Text(
                                    text = BuildConfig.VERSION_NAME,
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
