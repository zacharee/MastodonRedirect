package dev.zwander.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zwander.shared.components.LinkBlocklistLayout

class LinkBlocklistActivity : BaseActivity() {
    @Composable
    override fun Content() {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            LinkBlocklistLayout(
                modifier = Modifier.fillMaxSize()
                    .imePadding()
                    .systemBarsPadding(),
            )
        }
    }
}
