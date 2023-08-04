package dev.zwander.shared.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerifyUtils
import dev.zwander.shared.util.Prefs
import dev.zwander.shared.util.RedirectorTheme
import dev.zwander.shared.util.rememberPreferenceState

@Composable
fun MainContent() {
    val context = LocalContext.current
    val appModel = LocalAppModel.current
    val prefs = appModel.prefs

    var enableCrashReports by rememberPreferenceState(
        key = Prefs.ENABLE_CRASH_REPORTS,
        value = { prefs.enableCrashReports },
    ) { prefs.enableCrashReports = it }

    val (linksVerified, missingDomains, refresh) = LinkVerifyUtils.rememberLinkVerificationAsState()

    LaunchedEffect(key1 = enableCrashReports) {
        if (enableCrashReports) {
            Bugsnag.start(context)
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
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text = appModel.appName,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = appModel.versionName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }

                    AnimatedVisibility(visible = !linksVerified.value) {
                        LinkVerifyLayout(
                            refresh = refresh,
                            missingDomains = missingDomains,
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
                            .padding(horizontal = 8.dp),
                    )
                }
            }
        }
    }
}
