package dev.zwander.shared

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.shizuku.ShizukuService
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import dev.zwander.shared.util.prefs
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import kotlin.random.Random

val Context.app: App
    get() = (applicationContext ?: this) as App

abstract class App : Application() {
    abstract val versionCode: Int
    abstract val versionName: String
    abstract val appName: String
    abstract val launchStrategyUtils: BaseLaunchStrategyUtils
    abstract val defaultLaunchStrategy: LaunchStrategy

    val prefs by lazy { prefs(launchStrategyUtils, defaultLaunchStrategy) }

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
            ).version(versionCode + (if (BuildConfig.DEBUG) Random.nextInt() else 0))
                .processNameSuffix(":mastodon_redirect"),
            userServiceConnection,
        )
    }
}