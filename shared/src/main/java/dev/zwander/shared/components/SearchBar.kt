package dev.zwander.shared.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.zwander.shared.R

private enum class SearchState(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    CLOSED(
        icon = R.drawable.search_24px,
        label = R.string.search,
    ),
    OPEN(
        icon = R.drawable.keyboard_arrow_down_24px,
        label = R.string.close,
    ),
    SEARCHING(
        icon = R.drawable.backspace_24px,
        label = R.string.clear,
    );
}

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
                val searchState by remember {
                    derivedStateOf {
                        when {
                            isFocused && text.isNotEmpty() -> SearchState.SEARCHING
                            isFocused -> SearchState.OPEN
                            else -> SearchState.CLOSED
                        }
                    }
                }

                Crossfade(
                    targetState = searchState,
                    label = "SearchCrossfade",
                ) { state ->
                    IconButton(
                        onClick = {
                            when (state) {
                                SearchState.CLOSED -> {
                                    focusRequester.requestFocus()
                                }
                                SearchState.OPEN -> {
                                    focusManager.clearFocus()
                                }
                                SearchState.SEARCHING -> {
                                    onTextChange("")
                                }
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(state.icon),
                            contentDescription = stringResource(state.label),
                        )
                    }
                }
            },
            trailingIcon = {
                IconButton(onClick = onScrollToTop) {
                    Icon(
                        painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                        contentDescription = stringResource(id = R.string.scroll_to_top),
                    )
                }
            },
            shape = shape,
        )
    }
}
