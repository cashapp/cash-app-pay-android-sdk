package app.cash.paykit.core.android

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Singleton that holds onto our application [Context] as a [WeakReference]
 */
internal object ApplicationContextHolder {
  private var isInitialized: Boolean = false

  private lateinit var applicationContextReference: WeakReference<Context>

  fun init(applicationContext: Context) {
    if (isInitialized) {
      return
    }
    isInitialized = true
    applicationContextReference = WeakReference(applicationContext.applicationContext)
  }

  val applicationContext: Context
    get() = applicationContextReference.get()!!
}
