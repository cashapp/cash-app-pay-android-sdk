package com.squareup.cash.paykit

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.ref.WeakReference

object PayKitLifecycleObserver : DefaultLifecycleObserver {

  private val payKitInstances = arrayListOf<WeakReference<PayKitLifecycleListener>>()

  @VisibleForTesting
  var processLifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()

  /*
  * Functions to register & unregister instances of [PayKitLifecycleListener].
   */

  fun register(newInstance: PayKitLifecycleListener) {
    // Register for ProcessLifecycle changes if this is the first PayKitLifecycleListener.
    if (payKitInstances.isEmpty()) {
      processLifecycleOwner
        .lifecycle
        .addObserver(this)
    }

    payKitInstances.add(WeakReference(newInstance))
  }

  fun unregister(instanceToRemove: PayKitLifecycleListener) {
    val internalInstance = payKitInstances.firstOrNull { it.get() == instanceToRemove }
    payKitInstances.remove(internalInstance)

    // Stop listening for ProcessLifecycle changes if no more PayKitLifecycleListeners are available.
    if (payKitInstances.isEmpty()) {
      processLifecycleOwner
        .lifecycle
        .removeObserver(this)
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