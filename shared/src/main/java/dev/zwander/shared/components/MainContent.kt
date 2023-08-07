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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerifyUtils
import dev.zwander.shared.util.RedirectorTheme

@Composable
fun MainContent() {
    val appModel = LocalAppModel.current

    val verificationStatus by LinkVerifyUtils.rememberLinkVerificationAsState()

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
                        .fillMaxSize(),
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

                    AnimatedVisibility(visible = !verificationStatus.verified) {
                        LinkVerifyLayout(
                            refresh = verificationStatus.refresh,
                            missingDomains = verificationStatus.missingDomains,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppChooserLayout(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    FooterLayout(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
