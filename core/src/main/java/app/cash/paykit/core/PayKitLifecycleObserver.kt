package app.cash.paykit.core

import app.cash.paykit.core.impl.PayKitLifecycleListener

interface PayKitLifecycleObserver {
  fun register(newInstance: PayKitLifecycleListener)
  fun unregister(instanceToRemove: PayKitLifecycleListener)
}
