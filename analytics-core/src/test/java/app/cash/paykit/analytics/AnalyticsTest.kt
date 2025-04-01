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
package app.cash.paykit.analytics

import android.content.Context
import app.cash.paykit.analytics.Utils.getPrivateField
import app.cash.paykit.analytics.core.Deliverable
import app.cash.paykit.analytics.core.DeliveryHandler
import app.cash.paykit.analytics.core.DeliveryListener
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.EntriesDataSource
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSqLiteHelper
import app.cash.paykit.logging.CashAppLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
class AnalyticsTest {

  private var options = AnalyticsOptions(
    delay = 0.seconds,
    interval = 10.seconds,
    maxEntryCountPerProcess = 30,
    batchSize = 10,
    databaseName = "test.db",
  )

  private val analyticsSqLiteHelper: AnalyticsSqLiteHelper = mockk(relaxed = true)
  private val cashAppLogger: CashAppLogger = mockk(relaxed = true)
  private val entriesDataSource: EntriesDataSource = mockk(relaxed = true)
  private val app = RuntimeEnvironment.getApplication()

  @Before
  fun setup() {
    mockkStatic(Executors::class)
    val mockScheduledFuture = mockk<ScheduledFuture<Unit>>(relaxed = true)
    val mockExecutor: ScheduledExecutorService = mockk(relaxed = true)
    every { Executors.newSingleThreadScheduledExecutor() } returns mockExecutor

    every { mockExecutor.scheduleWithFixedDelay(any(), any(), any(), any()) } answers {
      val task = this.args.first() as Runnable
      task.run()
      mockScheduledFuture
    }
  }

  @After
  fun tearDown() {
    unmockkStatic(Executors::class)
  }

  @Test
  fun `test initialization`() {
    val payKitAnalytics = createPayKitAnalytics()
    val context = getPrivateField(payKitAnalytics, "context") as Context
    assertNotNull(context)

    val entriesDataSource: EntriesDataSource = getPrivateField(
      payKitAnalytics,
      "entriesDataSource",
    ) as EntriesDataSource
    assertNotNull(entriesDataSource)

    val options: AnalyticsOptions =
      getPrivateField(payKitAnalytics, "options") as AnalyticsOptions
    assertNotNull(options)

    val synchronizationTasks = getPrivateField(payKitAnalytics, "deliveryTasks") as List<*>
    assertNotNull(synchronizationTasks)
    assertEquals(1, synchronizationTasks.size) // the initial delivery task

    val deliveryHandlers =
      getPrivateField(payKitAnalytics, "deliveryHandlers") as ArrayList<*>
    assertNotNull(deliveryHandlers)
    assertEquals(0, deliveryHandlers.size)

    val executor = getPrivateField(payKitAnalytics, "executor") as ExecutorService
    assertNotNull(executor)
    assertFalse(executor.isShutdown)
    assertFalse(executor.isTerminated)

    val scheduler = getPrivateField(payKitAnalytics, "scheduler") as ScheduledExecutorService
    assertNotNull(scheduler)
    assertFalse(scheduler.isShutdown)
    assertFalse(scheduler.isTerminated)

    val shouldShutdown = getPrivateField(payKitAnalytics, "shouldShutdown") as AtomicBoolean
    assertFalse(shouldShutdown.get())
  }

  @Test
  fun `test initialization with delay`() {
    // uses the default scheduler so the delay is taken into acccount
    unmockkStatic(Executors::class)

    val noDelayOptions = AnalyticsOptions(
      delay = 5.seconds,
      interval = 10.seconds,
      maxEntryCountPerProcess = 30,
      batchSize = 10,
      databaseName = "test.db",
    )
    val payKitAnalytics = createPayKitAnalytics(noDelayOptions)

    val synchronizationTasks = getPrivateField(payKitAnalytics, "deliveryTasks") as List<*>
    assertNotNull(synchronizationTasks)
    assertEquals(0, synchronizationTasks.size)
  }

  @Test
  fun `test register delivery handlers`() {
    val payKitAnalytics = createPayKitAnalytics()
    val deliveryHandlers =
      getPrivateField(payKitAnalytics, "deliveryHandlers") as ArrayList<*>
    assertEquals(0, deliveryHandlers.size)

    val h1Type = "TYPE1"
    val h1 = object : DeliveryHandler() {
      override val deliverableType = h1Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }

    val h2Type = "TYPE2"
    val h2 = object : DeliveryHandler() {
      override val deliverableType = h2Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }

    payKitAnalytics.registerDeliveryHandler(h1)
    payKitAnalytics.registerDeliveryHandler(h2)
    assertEquals(2, deliveryHandlers.size)
  }

  @Test
  fun `register duplicate delivery handlers`() {
    val payKitAnalytics = createPayKitAnalytics()
    val deliveryHandlers =
      getPrivateField(payKitAnalytics, "deliveryHandlers") as ArrayList<*>
    assertEquals(0, deliveryHandlers.size)

    val h1Type = "TYPE1"
    val h1 = object : DeliveryHandler() {
      override val deliverableType = h1Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }

    val h2 = object : DeliveryHandler() {
      override val deliverableType = h1Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }

    payKitAnalytics.registerDeliveryHandler(h1)
    payKitAnalytics.registerDeliveryHandler(h2)
    assertEquals(1, deliveryHandlers.size)
  }

  @Test
  fun `get delivery handlers`() {
    val payKitAnalytics = createPayKitAnalytics()

    val h1Type = "TYPE1"
    assertNull(payKitAnalytics.getDeliveryHandler(h1Type))

    val h1 = object : DeliveryHandler() {
      override val deliverableType = h1Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }

    val h2Type = "TYPE2"
    val h2 = object : DeliveryHandler() {
      override val deliverableType = h2Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }
    payKitAnalytics.registerDeliveryHandler(h1)
    payKitAnalytics.registerDeliveryHandler(h2)

    with(payKitAnalytics.getDeliveryHandler(h1Type)) {
      assertNotNull(this)
      this as DeliveryHandler
      assertTrue(h1Type.equals(deliverableType, ignoreCase = true))
    }

    with(payKitAnalytics.getDeliveryHandler(h2Type)) {
      assertNotNull(this)
      this as DeliveryHandler
      assertTrue(h2Type.equals(deliverableType, ignoreCase = true))
    }
    assertNull(payKitAnalytics.getDeliveryHandler("NO_TYPE"))
  }

  @Test
  fun `schedule for delivery`() {
    val payKitAnalytics = createPayKitAnalytics()

    val h1Type = "TYPE1"

    val deliverable = object : Deliverable {
      override val type = h1Type
      override val content = "content"
      override val metaData = "meta"
    }
    val deliveryHandler = object : DeliveryHandler() {
      override val deliverableType = h1Type
      override fun deliver(entries: List<AnalyticEntry>, deliveryListener: DeliveryListener) =
        Unit
    }
    payKitAnalytics.registerDeliveryHandler(deliveryHandler)

    val task = payKitAnalytics.scheduleForDelivery(deliverable)
    task.get()

    val scheduler = getPrivateField(payKitAnalytics, "scheduler") as ScheduledExecutorService
    assertNotNull(scheduler)
    assertFalse(scheduler.isShutdown)

    val shouldShutdown = getPrivateField(payKitAnalytics, "shouldShutdown") as AtomicBoolean
    assertFalse(shouldShutdown.get())
    verify {
      entriesDataSource.insertEntry(
        eq(deliverable.type),
        eq(deliverable.content),
        eq(deliverable.metaData),
      )
    }
  }

  private fun createPayKitAnalytics(testOptions: AnalyticsOptions = options) = PayKitAnalytics(
    context = app,
    options = testOptions,
    sqLiteHelper = analyticsSqLiteHelper,
    cashAppLogger = cashAppLogger,
    entriesDataSource = entriesDataSource,
  )
}
