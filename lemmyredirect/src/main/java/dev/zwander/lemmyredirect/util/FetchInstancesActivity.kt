package dev.zwander.lemmyredirect.util

import dev.zwander.shared.BaseFetchActivity
import dev.zwander.shared.FetchedInstance
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream

@Serializable
private data class Instance(
    val base: String?,
)

class FetchInstancesActivity : BaseFetchActivity() {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun loadInstances(): List<FetchedInstance> {
        val response = HttpClient().get(
            urlString = "https://data.lemmyverse.net/data/instance.min.json",
        )

        val kbinResponse = HttpClient().get(
            urlString = "https://data.lemmyverse.net/data/kbin.min.json",
        )

        val root = json.decodeFromStream<List<Instance>>(response.body())
        val kbinRoot = json.decodeFromStream<List<String>>(kbinResponse.body()).map { Instance(it) }

        return (root + kbinRoot).filter {
            !it.base.isNullOrBlank() && !it.base.startsWith(".") && it.base.contains(".")
        }.distinctBy { it.base }.map { FetchedInstance(it.base!!, it.base) }
    }
}
