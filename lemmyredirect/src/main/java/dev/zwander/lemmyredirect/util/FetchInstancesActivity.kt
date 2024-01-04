package dev.zwander.lemmyredirect.util

import dev.zwander.shared.BaseFetchActivity

class FetchInstancesActivity : BaseFetchActivity() {
    override val softwareNames: Array<String> = arrayOf(
        "lemmy",
        "kbin",
        "mbin",
    )
}
