package com.squareup.cash.paykit.impl

interface PayKitLifecycleObserver {
  fun register(newInstance: PayKitLifecycleListener)
  fun unregister(instanceToRemove: PayKitLifecycleListener)
}
