package dev.zwander.shared.util

import android.view.animation.AnticipateOvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import dev.zwander.shared.R

@Composable
fun Expander(
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.elevatedCardColors(),
) {
    @Composable
    fun contents() {
        Box(
            modifier = modifier
                .clickable { onExpand(!expanded) },
            contentAlignment = Alignment.Center,
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(
                    durationMillis = integerResource(id = android.R.integer.config_longAnimTime),
                    easing = {
                        AnticipateOvershootInterpolator().getInterpolation(it)
                    }
                ),
                label = "Expander-Rotation",
            )

            Icon(
                painter = painterResource(id = R.drawable.baseline_keyboard_arrow_down_24),
                contentDescription = null,
                modifier = Modifier.rotate(rotation),
            )
        }
    }

    ElevatedCard(
        modifier = Modifier,
        shape = MaterialTheme.shapes.medium,
        colors = colors,
    ) {
        contents()
    }
}