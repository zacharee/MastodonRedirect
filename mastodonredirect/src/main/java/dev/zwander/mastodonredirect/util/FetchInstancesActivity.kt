package dev.zwander.mastodonredirect.util

import dev.zwander.mastodonredirect.BuildConfig
import dev.zwander.shared.BaseFetchActivity
import dev.zwander.shared.FetchedInstance
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class InstancesRoot(
    val instances: List<Instance>,
)

@Serializable
private data class Instance(
    val id: String,
    val name: String?,
    @SerialName("active_users")
    val activeUsers: String?,
    val admin: String?,
)

class FetchInstancesActivity : BaseFetchActivity() {
    override suspend fun loadInstances(): List<FetchedInstance> {
        val response = HttpClient {
            Auth {
                bearer {
                    loadTokens {
                        // https://instances.social/api/token
                        // Define `instances_social_key=YOUR_KEY` in local.properties.
                        BearerTokens(
                            BuildConfig.INSTANCES_SOCIAL_KEY,
                            "",
                        )
                    }
                }
            }
        }.get(
            urlString = "https://instances.social/api/1.0/instances/list?include_dead=false&include_down=true&include_closed=true&sort_by=name&count=0&min_active_users=0&min_users=1",
        )

        return json.decodeFromString<InstancesRoot>(response.body()).instances.mapNotNull { instance ->
            FetchedInstance(instance.id, instance.name).takeIf { instance.admin != null }
        }
    }
}
