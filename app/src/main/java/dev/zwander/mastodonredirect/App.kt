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
import kotlin.random.Random

class App : Application() {
    private val queuedCommands = ArrayList<IShizukuService.() -> Unit>()
    private var userService: IShizukuService? = null
        set(value) {
            field = value

            if (value != null) {
                queuedCommands.forEach {
                    it(value)
                }
                queuedCommands.clear()
            }
        }

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

        if (prefs.enableCrashReports) {
            Bugsnag.start(this)
        }

        Shizuku.addBinderReceivedListenerSticky {
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

    fun postShizukuCommand(command: IShizukuService.() -> Unit) {
        if (userService != null) {
            command(userService!!)
        } else {
            queuedCommands.add(command)
        }
    }

    private fun addUserService() {
        Shizuku.bindUserService(
            Shizuku.UserServiceArgs(
                ComponentName(this, ShizukuService::class.java)
            ).version(BuildConfig.VERSION_CODE + (if (BuildConfig.DEBUG) Random.nextInt() else 0))
                .processNameSuffix(":mastodon_redirect"),
            userServiceConnection,
        )
    }
}