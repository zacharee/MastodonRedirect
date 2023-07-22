package dev.zwander.mastodonredirect.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import dev.zwander.mastodonredirect.util.LaunchStrategy
import dev.zwander.mastodonredirect.util.LaunchStrategyGroup
import dev.zwander.mastodonredirect.util.Prefs
import dev.zwander.mastodonredirect.util.prefs
import dev.zwander.mastodonredirect.util.rememberPreferenceState
import dev.zwander.mastodonredirect.util.rememberSortedLaunchStrategies

@Composable
fun AppChooserLayout(
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
    ) {
        Column(
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

        LazyVerticalStaggeredGrid(
            modifier = Modifier,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp,
            columns = AdaptiveMod(minSize = 300.dp, itemCount = launchStrategies.size),
        ) {
            items(launchStrategies, { it.labelRes }) { strategy ->
                GroupCard(
                    strategyGroup = strategy,
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { selectedStrategy = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupCard(
    strategyGroup: LaunchStrategyGroup,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    OutlinedCard(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Text(
                text = with (strategyGroup) { context.label },
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.titleLarge,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                strategyGroup.children.forEach { child ->
                    SingleCard(
                        strategy = child,
                        selectedStrategy = selectedStrategy,
                        onStrategySelected = onStrategySelected,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleCard(
    strategy: LaunchStrategy,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (selectedStrategy == strategy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        label = "CardColor-${strategy.key}",
    )

    ElevatedCard(
        onClick = { onStrategySelected(strategy) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = color,
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
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
