package dev.zwander.shared.util

import android.content.ComponentName
import android.content.pm.ActivityInfo

val ActivityInfo.componentNameCompat: ComponentName
    get() = ComponentName(packageName, name)
