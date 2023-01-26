package app.cash.paykit.devapp

import android.app.Application

class DevApplication : Application() {

  companion object {
    lateinit var instance: DevApplication
  }

  override fun onCreate() {
    super.onCreate()
    instance = this
  }
}
