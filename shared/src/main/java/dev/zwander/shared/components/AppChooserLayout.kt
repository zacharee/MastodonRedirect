package dev.zwander.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup
import dev.zwander.shared.R
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.rememberMutablePreferenceState

@Composable
fun AppChooserLayout(
    modifier: Modifier = Modifier,
) {
    val appModel = LocalAppModel.current
    val prefs = appModel.prefs
    val launchStrategies = appModel.launchStrategyUtils.rememberSortedLaunchStrategies()

    var selectedStrategy by prefs.selectedApp.rememberMutablePreferenceState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                text = stringResource(id = R.string.choose_app),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(id = R.string.choose_app_desc, appModel.appName),
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
            items(launchStrategies, { it.labelRes }) { strategyGroup ->
                GroupCard(
                    strategyGroup = strategyGroup,
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
    strategyGroup: LaunchStrategyRootGroup,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (strategyGroup.children.size > 1) {
                Column {
                    GroupTitle(strategyGroup = strategyGroup)

                    GroupRow(
                        strategyGroup = strategyGroup,
                        selectedStrategy = selectedStrategy,
                        onStrategySelected = onStrategySelected,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    GroupTitle(
                        strategyGroup = strategyGroup,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                    )

                    GroupRow(
                        strategyGroup = strategyGroup,
                        selectedStrategy = selectedStrategy,
                        onStrategySelected = onStrategySelected,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(
    strategyGroup: LaunchStrategyRootGroup,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Text(
        text = with (strategyGroup) { context.label },
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupRow(
    strategyGroup: LaunchStrategyRootGroup,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleCard(
    strategy: LaunchStrategy,
    selectedStrategy: LaunchStrategy,
    onStrategySelected: (LaunchStrategy) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color by animateColorAsState(
        targetValue = if (selectedStrategy == strategy) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        label = "SingleCardColor-${strategy.key}",
    )

    val textColor by animateColorAsState(
        targetValue = if (selectedStrategy == strategy) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        },
        label = "SingleCardText-${strategy.key}"
    )

    ElevatedCard(
        onClick = { onStrategySelected(strategy) },
        colors = CardDefaults.elevatedCardColors(
            containerColor = color,
        ),
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
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
                color = textColor,
            )
        }
    }
}
