package dev.zwander.mastodonredirect.util

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
import dev.zwander.mastodonredirect.ui.theme.MastodonRedirectTheme
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@Serializable
private data class InstancesRoot(
    val instances: List<Instance>,
)

@Serializable
private data class Instance(
    val id: String,
    val name: String?,
)

class FetchInstancesActivity : ComponentActivity() {
    @SuppressLint("Recycle")
    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        setContent {
            var items by remember {
                mutableStateOf(listOf<Instance>())
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
                val response = HttpClient {
                    Auth {
                        bearer {
                            loadTokens {
                                // https://instances.social/api/token
                                BearerTokens(
                                    "TOKEN_HERE",
                                    "",
                                )
                            }
                        }
                    }
                }.get(
                    urlString = "https://instances.social/api/1.0/instances/list?include_dead=false&include_down=true&include_closed=true&sort_by=name&count=0&min_active_users=1",
                )

                val instancesRoot = json.decodeFromStream<InstancesRoot>(response.body())

                items = instancesRoot.instances.filter {
                    !it.name.isNullOrBlank() && !it.name.startsWith(".") && it.name.contains(".")
                }.distinctBy { it.name }
            }

            MastodonRedirectTheme {
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
}
