package dev.zwander.peertuberedirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Array<String> = arrayOf(
        "peertube",
    )
}
