package dev.zwander.shared

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import dev.zwander.shared.util.RedirectorTheme
import kotlinx.serialization.json.Json

data class FetchedInstance(
    val id: String,
    val name: String?,
)

abstract class BaseFetchActivity : ComponentActivity() {
    protected val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            var items by remember {
                mutableStateOf(listOf<FetchedInstance>())
            }

            val exportResult = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument("text/xml")) { uri ->
                uri?.let {
                    contentResolver.openOutputStream(uri, "w")?.bufferedWriter()?.use { output ->
                        items.forEach { item ->
                            output.write("<data android:host=\"${item.name}\" />\n")
                        }
                    }
                }
            }

            LaunchedEffect(key1 = null) {
                items = loadInstances().filter {
                    !it.name.isNullOrBlank() && !it.name.startsWith(".") && it.name.contains(".")
                }.distinctBy { it.name }
            }

            RedirectorTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Button(onClick = {
                            exportResult.launch("instances.xml")
                        }) {
                            Text(text = "Export")
                        }

                        SelectionContainer {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                            ) {
                                items(items, { it.id }) {
                                    Text(text = "<data android:host=\"${it.name}\" />")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected abstract suspend fun loadInstances(): List<FetchedInstance>
}