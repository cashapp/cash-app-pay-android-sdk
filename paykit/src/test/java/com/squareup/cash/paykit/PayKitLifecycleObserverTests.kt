package com.squareup.cash.paykit

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.mockk.every
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
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)
    PayKitLifecycleObserver.register(listenerMock)

    // Simulate Application Lifecycle events.
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    verify(exactly = 1) { listenerMock.onApplicationForegrounded() }
  }

  @Test
  fun `after unRegister PayKitLifecycleListener will NOT receive updates`() = runTest {
    val testLifecycleOwner = TestLifecycleOwner()
    PayKitLifecycleObserver.processLifecycleOwner = testLifecycleOwner
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)

    // Register and unregister listener.
    PayKitLifecycleObserver.register(listenerMock)
    PayKitLifecycleObserver.unregister(listenerMock)

    // Simulate Application Lifecycle events.
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

    verify(exactly = 0) { listenerMock.onApplicationForegrounded() }
    verify(exactly = 0) { listenerMock.onApplicationBackgrounded() }
  }

  @Test
  fun `removeObserver should be called when all payKitInstances are gone`() {
    val mockLifecycleOwner = mockk<LifecycleOwner>(relaxed = true)
    val mockLifecycle = mockk<Lifecycle>(relaxed = true)
    every { mockLifecycleOwner.lifecycle } returns mockLifecycle
    PayKitLifecycleObserver.processLifecycleOwner = mockLifecycleOwner

    // Register and unregister a mock listener.
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)
    PayKitLifecycleObserver.register(listenerMock)
    verify(exactly = 0) { mockLifecycle.removeObserver(any()) }

    PayKitLifecycleObserver.unregister(listenerMock)
    verify(atLeast = 1) { mockLifecycle.removeObserver(any()) }
  }
}