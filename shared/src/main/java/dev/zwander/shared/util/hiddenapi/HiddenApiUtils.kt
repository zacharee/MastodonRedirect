package dev.zwander.shared.util.hiddenapi

import android.annotation.SuppressLint
import android.os.IBinder
import android.os.UserHandle

object HiddenApiUtils {
    fun Class<*>.asInterface(obj: IBinder): Any {
        return this.getMethod("asInterface", IBinder::class.java)
            .invoke(null, obj)!!
    }

    val currentUserId: Int
        @SuppressLint("PrivateApi")
        get() = UserHandle::class.java
            .getDeclaredMethod("myUserId")
            .invoke(null) as Int
}
