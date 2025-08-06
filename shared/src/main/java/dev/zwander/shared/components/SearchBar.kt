package dev.zwander.shared.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import dev.zwander.shared.R

@Composable
fun SearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    onScrollToTop: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember {
        FocusRequester()
    }

    var isFocused by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .wrapContentHeight()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = shape,
            ),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { isFocused = it.isFocused },
            leadingIcon = {
                val buttonProps: Triple<() -> Unit, ImageVector, Int> = when {
                    isFocused && text.isNotEmpty() -> {
                        Triple({ onTextChange("") }, Icons.Default.Clear, R.string.clear)
                    }
                    isFocused -> {
                        Triple({ focusManager.clearFocus() }, Icons.Default.KeyboardArrowDown, R.string.close)
                    }
                    else -> {
                        Triple({ focusRequester.requestFocus() }, Icons.Default.Search, R.string.search)
                    }
                }

                Crossfade(
                    targetState = buttonProps,
                    label = "SearchCrossfade",
                ) { (clickAction, image, desc) ->
                    IconButton(
                        onClick = clickAction,
                    ) {
                        Icon(
                            imageVector = image,
                            contentDescription = stringResource(id = desc),
                        )
                    }
                }
            },
            trailingIcon = {
                IconButton(onClick = onScrollToTop) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = stringResource(id = R.string.scroll_to_top),
                    )
                }
            },
            shape = shape,
        )
    }
}
