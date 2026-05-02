package dev.zwander.mastodonredirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Map<String, Boolean> = mapOf(
        "mastodon" to false,
        "calckey" to false,
        "misskey" to false,
        "pleroma" to false,
        "friendica" to false,
        "catodon" to false,
        "akkoma" to false,
        "gotosocial" to false,
        "firefish" to false,
        "diaspora" to false,
        "hometown" to false,
        "iceshrimp" to false,
        "hubzilla" to false,
        "nextcloud social" to false,
        "pixelfed" to false,
        "sharkey" to false,
        "wildebeest" to false,
        "bridgyfed" to true,
        "redmatrix" to false,
        "socialhome" to false,
        "writefreely" to false,
        "plume" to false,
        "mostr" to true,
        "loops" to false,
        "ghost" to false,
        "foundkey" to false,
        "birdsitelive" to true,
    )
}
