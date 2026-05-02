package dev.zwander.peertuberedirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Map<String, Boolean> = mapOf(
        "peertube" to false,
    )
}
