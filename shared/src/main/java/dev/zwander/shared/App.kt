package dev.zwander.shared

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.model.AppModel
import dev.zwander.shared.shizuku.ShizukuService
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import dev.zwander.shared.util.Prefs
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku

val Context.app: App
    get() = (applicationContext ?: this) as App

val Context.appModel: AppModel
    get() = app

abstract class App(
    override val launchStrategyUtils: BaseLaunchStrategyUtils,
    override val defaultLaunchStrategy: LaunchStrategy,
) : Application(), AppModel {
    private val pInfo by lazy {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }

    override val versionName by lazy {
        pInfo.versionName.toString()
    }
    override val appName by lazy {
        pInfo.applicationInfo.loadLabel(packageManager).toString()
    }

    override val prefs: Prefs
        get() = Prefs.getInstance(this)

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

    @Suppress("DEPRECATION")
    private val serviceArgs by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, ShizukuService::class.java.canonicalName))
            .version(pInfo.versionCode + (if (BuildConfig.DEBUG) 9999 else 0))
            .processNameSuffix("redirect")
            .debuggable(BuildConfig.DEBUG)
            .daemon(false)
            .tag("${packageName}_redirect")
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

    override fun postShizukuCommand(command: IShizukuService.() -> Unit) {
        if (userService != null) {
            command(userService!!)
        } else {
            queuedCommands.add(command)
        }
    }

    private fun addUserService() {
        Shizuku.unbindUserService(
            serviceArgs,
            userServiceConnection,
            true
        )

        Shizuku.bindUserService(
            serviceArgs,
            userServiceConnection,
        )
    }
}