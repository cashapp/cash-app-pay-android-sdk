package app.cash.paykit.core.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.cash.paykit.core.CashAppPayKitFactory
import app.cash.paykit.core.PayKitLifecycleObserver
import java.lang.ref.WeakReference

/**
 * This class is intended to be a singleton.
 * The [CashAppPayKitFactory] static object creates and holds onto this single instance.
 */
internal class PayKitLifecycleObserverImpl(
  private val processLifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get(),
) : DefaultLifecycleObserver,
  PayKitLifecycleObserver {

  private val payKitInstances = arrayListOf<WeakReference<PayKitLifecycleListener>>()

  private var mainHandler: Handler = Handler(Looper.getMainLooper())

  /*
  * Functions to register & unregister instances of [PayKitLifecycleListener].
   */

  override fun register(newInstance: PayKitLifecycleListener) {
    // Register for ProcessLifecycle changes if this is the first PayKitLifecycleListener.
    if (payKitInstances.isEmpty()) {
      runOnUiThread {
        processLifecycleOwner
          .lifecycle
          .addObserver(this)
      }
    }

    payKitInstances.add(WeakReference(newInstance))
  }

  override fun unregister(instanceToRemove: PayKitLifecycleListener) {
    val internalInstance = payKitInstances.firstOrNull { it.get() == instanceToRemove }
    payKitInstances.remove(internalInstance)

    // Stop listening for ProcessLifecycle changes if no more PayKitLifecycleListeners are available.
    if (payKitInstances.isEmpty()) {
      runOnUiThread {
        processLifecycleOwner
          .lifecycle
          .removeObserver(this)
      }
    }
  }

  private fun runOnUiThread(action: Runnable) {
    val isOnMainThread = Looper.getMainLooper().thread == Thread.currentThread()
    if (isOnMainThread) {
      action.run()
    } else {
      mainHandler.post(action)
    }
  }

  /*
  * Callback functions from [DefaultLifecycleObserver].
   */

  override fun onStart(owner: LifecycleOwner) {
    super.onStart(owner)
    payKitInstances.forEach { it.get()?.onApplicationForegrounded() }
  }

  override fun onStop(owner: LifecycleOwner) {
    super.onStop(owner)
    payKitInstances.forEach { it.get()?.onApplicationBackgrounded() }
  }
}

/**
 * Interface that exposes process foreground/background callback functions.
 */
interface PayKitLifecycleListener {
  /**
   * Triggered when the application process was foregrounded.
   */
  fun onApplicationForegrounded()

  /**
   * Triggered when the application process was backgrounded.
   */
  fun onApplicationBackgrounded()
}
