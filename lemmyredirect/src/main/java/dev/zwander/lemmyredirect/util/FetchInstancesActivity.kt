package dev.zwander.lemmyredirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Map<String, Boolean> = mapOf(
        "lemmy" to false,
        "kbin" to false,
        "mbin" to false,
        "piefed" to false,
    )
}
