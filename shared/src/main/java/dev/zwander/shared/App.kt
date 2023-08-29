package dev.zwander.shared

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.StringRes
import com.bugsnag.android.Bugsnag
import dev.zwander.shared.model.AppModel
import dev.zwander.shared.shizuku.ShizukuService
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import dev.zwander.shared.util.Prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import kotlin.coroutines.CoroutineContext

val Context.app: App
    get() = (applicationContext ?: this) as App

val Context.appModel: AppModel
    get() = app

abstract class App(
    override val launchStrategyUtils: BaseLaunchStrategyUtils,
    override val fetchActivity: Class<*>,
    override val defaultLaunchStrategy: LaunchStrategy,
    @StringRes private val appNameRes: Int,
) : Application(), AppModel, CoroutineScope by MainScope() {
    override val appName by lazy {
        resources.getString(appNameRes)
    }
    override val prefs: Prefs
        get() = Prefs.getInstance(this)

    private val queuedCommands = ArrayList<Pair<CoroutineContext, IShizukuService.() -> Unit>>()
    private var userService: IShizukuService? = null
        set(value) {
            field = value

            if (value != null) {
                queuedCommands.forEach { (context, command) ->
                    launch(context) {
                        value.command()
                    }
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

    private val serviceArgs by lazy {
        Shizuku.UserServiceArgs(ComponentName(packageName, ShizukuService::class.java.canonicalName!!))
            .version(BuildConfig.VERSION_CODE + (if (BuildConfig.DEBUG) 10003 else 0))
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

        if (prefs.enableCrashReports.currentValue(this)) {
            Bugsnag.start(this)
        }

        Shizuku.addBinderReceivedListenerSticky {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                addUserService()
            } else {
                Shizuku.addRequestPermissionResultListener { _, grantResult ->
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        addUserService()
                    }
                }
            }
        }
    }

    override fun postShizukuCommand(context: CoroutineContext, command: IShizukuService.() -> Unit) {
        if (userService != null) {
            launch(context) {
                command(userService!!)
            }
        } else {
            queuedCommands.add(context to command)
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