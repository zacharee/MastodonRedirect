package dev.zwander.shared.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.shared.BuildConfig
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.LinkVerificationModel
import dev.zwander.shared.util.LinkVerifyUtils

@Composable
fun MainContent() {
    val appModel = LocalAppModel.current
    val context = LocalContext.current

    val verificationStatus by LinkVerifyUtils.rememberLinkVerificationAsState()

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

                    var clickCount by remember {
                        mutableIntStateOf(0)
                    }

                    Text(
                        text = BuildConfig.VERSION_NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                },
                                indication = null,
                                onClick = {
                                    if (clickCount < 9) {
                                        clickCount++
                                    } else {
                                        clickCount = 0
                                        context.startActivity(
                                            Intent(context, appModel.fetchActivity)
                                                .setAction(Intent.ACTION_MAIN)
                                        )
                                    }
                                },
                            ),
                        textAlign = TextAlign.Center,
                    )
                }

                AnimatedVisibility(visible = !verificationStatus.verified) {
                    LinkVerifyLayout(
                        refresh = LinkVerificationModel::refresh,
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
