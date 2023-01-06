package com.squareup.cash.paykit

import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.ref.WeakReference

//  todo make an interface, but use the static one when construction graph
object PayKitLifecycleObserver : DefaultLifecycleObserver {

  private val payKitInstances = arrayListOf<WeakReference<PayKitLifecycleListener>>()

  private var mainHandler: Handler = Handler(Looper.getMainLooper())

  @VisibleForTesting
  var processLifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()

  /**
   * Reset the state of this class, so that Unit Tests can avoid side-effects.
   */
  @VisibleForTesting
  fun reset() {
    payKitInstances.clear()
    mainHandler = Handler(Looper.getMainLooper())
  }

  /*
  * Functions to register & unregister instances of [PayKitLifecycleListener].
   */

  fun register(newInstance: PayKitLifecycleListener) {
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

  fun unregister(instanceToRemove: PayKitLifecycleListener) {
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
