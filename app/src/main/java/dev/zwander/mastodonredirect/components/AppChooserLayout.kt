package dev.zwander.mastodonredirect.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.zwander.mastodonredirect.R
import dev.zwander.mastodonredirect.util.Prefs
import dev.zwander.mastodonredirect.util.prefs
import dev.zwander.mastodonredirect.util.rememberSortedLaunchStrategies
import dev.zwander.mastodonredirect.util.rememberPreferenceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.AppChooserLayout(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val launchStrategies = rememberSortedLaunchStrategies()

    var selectedStrategy by context.rememberPreferenceState(
        key = Prefs.SELECTED_APP,
        value = { context.prefs.selectedApp },
        onChanged = { context.prefs.selectedApp = it },
    )

    Column(
        modifier = modifier,
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
            .weight(1f)
            .heightIn(max = 512.dp),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(launchStrategies.toList(), { it.key }) { strategy ->
            val color by animateColorAsState(
                targetValue = if (selectedStrategy == strategy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                label = "CardColor-${strategy.key}",
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

