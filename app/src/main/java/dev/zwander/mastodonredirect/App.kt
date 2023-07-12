package dev.zwander.mastodonredirect

import android.app.Application
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.bugsnag.android.Bugsnag
import dev.zwander.mastodonredirect.shizuku.ShizukuService
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku

class App : Application() {
    var userService: IShizukuService? = null
        private set

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            userService = IShizukuService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            userService = null
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        }

        Bugsnag.start(this)

        Shizuku.addBinderReceivedListener {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                addUserService()
            } else {
                Shizuku.addRequestPermissionResultListener(
                    object : Shizuku.OnRequestPermissionResultListener {
                        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                addUserService()
                                Shizuku.removeRequestPermissionResultListener(this)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun addUserService() {
        Shizuku.bindUserService(
            Shizuku.UserServiceArgs(
                ComponentName(this, ShizukuService::class.java)
            ).version(BuildConfig.VERSION_CODE)
                .processNameSuffix(":mastodon_redirect"),
            userServiceConnection,
        )
    }
}