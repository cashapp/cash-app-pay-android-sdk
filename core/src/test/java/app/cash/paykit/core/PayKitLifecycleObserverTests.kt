package app.cash.paykit.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import app.cash.paykit.core.impl.PayKitLifecycleListener
import app.cash.paykit.core.impl.PayKitLifecycleObserverImpl
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PayKitLifecycleObserverTests {

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @Test
  fun `registered PayKitLifecycleListener will receive updates`() = runTest {
    val testLifecycleOwner = TestLifecycleOwner()
    val payKitLifecycleObserver = PayKitLifecycleObserverImpl(testLifecycleOwner)

    // Create and register listener.
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)
    payKitLifecycleObserver.register(listenerMock)

    // Simulate Application Lifecycle events.
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
    testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

    verify(exactly = 1) { listenerMock.onApplicationForegrounded() }
  }

  @Test
  fun `after unRegister PayKitLifecycleListener will NOT receive updates`() = runTest {
    val testLifecycleOwner = TestLifecycleOwner()
    val payKitLifecycleObserver = PayKitLifecycleObserverImpl(testLifecycleOwner)
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)

    // Register and unregister listener.
    payKitLifecycleObserver.register(listenerMock)
    payKitLifecycleObserver.unregister(listenerMock)

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
    val payKitLifecycleObserver = PayKitLifecycleObserverImpl(mockLifecycleOwner)

    // Register and unregister a mock listener.
    val listenerMock = mockk<PayKitLifecycleListener>(relaxed = true)
    payKitLifecycleObserver.register(listenerMock)
    verify(exactly = 0) { mockLifecycle.removeObserver(any()) }

    payKitLifecycleObserver.unregister(listenerMock)
    verify(atLeast = 1) { mockLifecycle.removeObserver(any()) }
  }
}
