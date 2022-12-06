package com.squareup.cash.paykit

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PayKitLifecycleObserverTests {

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @Test
  fun `registered PayKitLifecycleListener will receive updates`() = runTest {
    val testLifecycleOwner = TestLifecycleOwner()
    PayKitLifecycleObserver.processLifecycleOwner = testLifecycleOwner

    // Create and register listener.
    val listenerSpy = mockk<PayKitLifecycleListener>(relaxed = true)
    PayKitLifecycleObserver.register(listenerSpy)

    // Simulate Application Lifecycle events.
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    verify(exactly = 1) { listenerSpy.onApplicationForegrounded() }
  }

  @Test
  fun `after unRegister PayKitLifecycleListener will NOT receive updates`() = runTest {
    val testLifecycleOwner = TestLifecycleOwner()
    PayKitLifecycleObserver.processLifecycleOwner = testLifecycleOwner
    val listenerSpy = mockk<PayKitLifecycleListener>(relaxed = true)

    // Register and unregister listener.
    PayKitLifecycleObserver.register(listenerSpy)
    PayKitLifecycleObserver.unregister(listenerSpy)

    // Simulate Application Lifecycle events.
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

    verify(exactly = 0) { listenerSpy.onApplicationForegrounded() }
    verify(exactly = 0) { listenerSpy.onApplicationBackgrounded() }
  }
}