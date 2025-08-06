package dev.zwander.shared

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

sealed interface LaunchIntentCreator {
    fun Context.createIntents(url: String): List<Intent>

    sealed interface ComponentIntentCreator : LaunchIntentCreator {
        val pkg: String
        val component: String
        val urlTransform: (String) -> String

        data class ViewIntentCreator(
            override val pkg: String,
            override val component: String,
            val scheme: String = "https",
            override val urlTransform: (String) -> String = { it },
        ) : ComponentIntentCreator {
            override fun Context.createIntents(url: String): List<Intent> {
                return listOf(
                    appModel.launchStrategyUtils.createViewIntent(
                        pkg = pkg,
                        component = component,
                        url = urlTransform(url),
                        scheme = scheme,
                    ),
                )
            }
        }

        data class ShareIntentCreator(
            override val pkg: String,
            override val component: String,
            val type: String = "*/*",
            override val urlTransform: (String) -> String = { it },
        ) : ComponentIntentCreator {
            override fun Context.createIntents(url: String): List<Intent> {
                return listOf(
                    appModel.launchStrategyUtils.createShareIntent(
                        pkg = pkg,
                        component = component,
                        url = urlTransform(url),
                        type = type,
                    ),
                )
            }
        }
    }

    data class BaseUrlIntentCreator(
        val baseUrl: String,
    ) : LaunchIntentCreator {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW, "$baseUrl$url".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    data class CustomIntentCreator(
        val creator: Context.(url: String) -> List<Intent>,
    ) : LaunchIntentCreator {
        override fun Context.createIntents(url: String): List<Intent> {
            return creator(url)
        }
    }

    data class DiscoveredIntentCreator(
        val components: List<ComponentName>,
        val launchAction: String,
    ) : LaunchIntentCreator {
        override fun Context.createIntents(url: String): List<Intent> {
            return components.map { cmp ->
                Intent(launchAction).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)

                    `package` = packageName
                    component = cmp
                    data = url.toUri()
                }
            }
        }
    }
}
