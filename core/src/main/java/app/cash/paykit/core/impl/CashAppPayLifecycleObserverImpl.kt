/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paykit.core.impl

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.cash.paykit.core.CashAppPayFactory
import app.cash.paykit.core.CashAppPayLifecycleObserver
import java.lang.ref.WeakReference

/**
 * This class is intended to be a singleton.
 * The [CashAppPayFactory] static object creates and holds onto this single instance.
 */
internal class CashAppPayLifecycleObserverImpl(
  private val processLifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get(),
) : DefaultLifecycleObserver,
  CashAppPayLifecycleObserver {

  private val payKitInstances = arrayListOf<WeakReference<CashAppPayLifecycleListener>>()

  private var mainHandler: Handler = Handler(Looper.getMainLooper())

  /*
  * Functions to register & unregister instances of [PayKitLifecycleListener].
   */

  override fun register(newInstance: CashAppPayLifecycleListener) {
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

  override fun unregister(instanceToRemove: CashAppPayLifecycleListener) {
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
interface CashAppPayLifecycleListener {
  /**
   * Triggered when the application process was foregrounded.
   */
  fun onApplicationForegrounded()

  /**
   * Triggered when the application process was backgrounded.
   */
  fun onApplicationBackgrounded()
}
