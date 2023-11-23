package dev.zwander.shared

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri

sealed interface LaunchIntentCreator {
    val transformUrl: (String) -> String

    fun Context.createIntents(url: String): List<Intent>

    sealed interface ComponentIntentCreator : LaunchIntentCreator {
        val pkg: String
        val component: String

        data class ViewIntentCreator(
            override val pkg: String,
            override val component: String,
            override val transformUrl: (String) -> String = { it },
        ) : ComponentIntentCreator {
            override fun Context.createIntents(url: String): List<Intent> {
                return listOf(
                    appModel.launchStrategyUtils.createViewIntent(
                        pkg = pkg,
                        component = component,
                        url = url,
                    ),
                )
            }
        }

        data class ShareIntentCreator(
            override val pkg: String,
            override val component: String,
            override val transformUrl: (String) -> String = { it },
        ) : ComponentIntentCreator {
            override fun Context.createIntents(url: String): List<Intent> {
                return listOf(
                    appModel.launchStrategyUtils.createShareIntent(
                        pkg = pkg,
                        component = component,
                        url = url,
                    ),
                )
            }
        }
    }

    data class BaseUrlIntentCreator(
        val baseUrl: String,
        override val transformUrl: (String) -> String = { it },
    ) : LaunchIntentCreator {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(Intent(Intent.ACTION_VIEW, Uri.parse("$baseUrl/$url")))
        }
    }

    data class CustomIntentCreator(
        override val transformUrl: (String) -> String = { it },
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
        override val transformUrl: (String) -> String = { it }

        override fun Context.createIntents(url: String): List<Intent> {
            return components.map { cmp ->
                Intent(launchAction).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)

                    `package` = packageName
                    component = cmp
                    data = Uri.parse(url)
                }
            }
        }
    }
}
