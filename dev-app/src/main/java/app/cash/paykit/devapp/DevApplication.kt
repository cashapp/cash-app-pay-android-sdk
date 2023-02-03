package app.cash.paykit.devapp

import android.app.Application
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.StrictMode
import android.os.StrictMode.VmPolicy.Builder
import android.os.strictmode.Violation
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors

class DevApplication : Application() {

  companion object {
    lateinit var instance: DevApplication
  }

  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()
          .penaltyLog()
          .build(),
      )

      if (VERSION.SDK_INT >= VERSION_CODES.P) {
        StrictMode.setVmPolicy(
          Builder()
            .detectAll()
            .withCustomDeathPenalty()
            .build(),
        )
      }
    }

    instance = this
  }

  @RequiresApi(VERSION_CODES.P)
  fun Builder.withCustomDeathPenalty(): Builder {
    return penaltyListener(
      Executors.newSingleThreadExecutor(),
      StrictMode.OnVmViolationListener {
        if (it.ignore()) {
          Log.d("PayKitPenalty", "ignoring penalty $it")
          return@OnVmViolationListener
        }
        throw it
      },
    )
  }

  private fun Violation.ignore(): Boolean {
    return isChucker() or isOkhttpTag()
  }

  // https://github.com/ChuckerTeam/chucker/issues/811
  private fun Violation.isChucker(): Boolean {
    return stackTrace[2].toString()
      .startsWith("sun.nio.fs.UnixSecureDirectoryStream.finalize(UnixSecureDirectoryStream")
  }

  // https://github.com/square/okhttp/issues/3537
  private fun Violation.isOkhttpTag(): Boolean {
    return stackTrace[0].toString().startsWith("android.os.StrictMode.onUntaggedSocket")
  }
}
