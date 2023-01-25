package app.cash.paykit.core

import android.content.Context
import androidx.startup.Initializer
import app.cash.paykit.core.android.ApplicationContextHolder

internal interface PayKitInitializerStub

internal class PayKitInitializer : Initializer<PayKitInitializerStub> {
  override fun create(context: Context): PayKitInitializerStub {
    ApplicationContextHolder.init(context.applicationContext)
    return object : PayKitInitializerStub {}
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}
