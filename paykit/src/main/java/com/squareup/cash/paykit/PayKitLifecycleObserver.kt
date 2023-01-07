package com.squareup.cash.paykit

import com.squareup.cash.paykit.impl.PayKitLifecycleListener

interface PayKitLifecycleObserver {
  fun register(newInstance: PayKitLifecycleListener)
  fun unregister(instanceToRemove: PayKitLifecycleListener)
}
