package dev.zwander.shared

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.apollographql.apollo3.ApolloClient
import dev.zwander.shared.generated.GetInstancesQuery
import java.util.TreeSet

data class FetchedInstance(
    val name: String,
) : Comparable<FetchedInstance> {
    override fun compareTo(other: FetchedInstance): Int {
        return name.compareTo(other.name)
    }
}

abstract class BaseFetchActivity : BaseActivity() {
    protected val client by lazy {
        ApolloClient.Builder()
            .serverUrl("https://api.fediverse.observer/")
            .build()
    }

    protected abstract val softwareNames: Array<String>

    @Composable
    override fun Content() {
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
                it.name.isNotBlank() && !it.name.startsWith(".") && it.name.contains(".")
            }
        }

        Surface {
            Column(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding(),
            ) {
                Button(onClick = {
                    exportResult.launch("${appModel.appName.replace(" ", "")}_instances.xml")
                }) {
                    Text(text = "Export")
                }

                SelectionContainer {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                    ) {
                        items(items, { it.name }) {
                            Text(text = "<data android:host=\"${it.name}\" />")
                        }
                    }
                }
            }
        }
    }

    protected suspend fun loadInstances(): List<FetchedInstance> {
        val list = TreeSet<FetchedInstance>()

        val response = client.query(GetInstancesQuery()).execute()

        response.data?.nodes?.mapNotNull { node ->
            if (node == null) {
                return@mapNotNull null
            }

            if (node.softwarename == null || !softwareNames.contains(node.softwarename)) {
                return@mapNotNull null
            }

            if (node.active_users_monthly == null || node.active_users_monthly <= 0) {
                return@mapNotNull null
            }

//            if (node.date_diedoff != null) {
//                return@mapNotNull null
//            }

            if (node.domain == null) {
                return@mapNotNull null
            }

            if (node.total_users == null || node.total_users < 1) {
                return@mapNotNull null
            }

            if (node.status != 1 && node.status != 5) {
                return@mapNotNull null
            }

            if (node.status != 5 && (node.score == null || node.score < 1)) {
                return@mapNotNull null
            }

            if (node.uptime_alltime == null || node.uptime_alltime.toFloat() < 70) {
                return@mapNotNull null
            }

            if (!node.sslvalid.isNullOrBlank() && node.sslvalid != "true") {
                return@mapNotNull null
            }

            if (node.local_posts == null || node.local_posts < 1) {
                return@mapNotNull null
            }

            if (node.protocols == null || !node.protocols.contains("activitypub")) {
                return@mapNotNull null
            }

            FetchedInstance(node.domain)
        }?.let { instances ->
            list.addAll(instances)
        }

        return list.toList()
    }
}