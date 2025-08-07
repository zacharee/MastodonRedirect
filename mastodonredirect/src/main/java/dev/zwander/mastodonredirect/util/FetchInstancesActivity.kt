package dev.zwander.mastodonredirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Array<String> = arrayOf(
        "mastodon",
        "calckey",
        "misskey",
        "pleroma",
        "friendica",
        "catodon",
        "akkoma",
        "gotosocial",
        "firefish",
        "diaspora",
        "hometown",
        "iceshrimp",
        "hubzilla",
        "nextcloud social",
        "pixelfed",
        "sharkey",
        "wildebeest",
        "bridgyfed",
    )
}
