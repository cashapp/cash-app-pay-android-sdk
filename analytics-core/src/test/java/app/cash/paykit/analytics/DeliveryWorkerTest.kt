/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package app.cash.paykit.analytics

import app.cash.paykit.analytics.core.DeliveryHandler
import app.cash.paykit.analytics.core.DeliveryListener
import app.cash.paykit.analytics.core.DeliveryWorker
import app.cash.paykit.analytics.persistence.AnalyticEntry
import app.cash.paykit.analytics.persistence.AnalyticEntry.Companion.STATE_DELIVERY_FAILED
import app.cash.paykit.analytics.persistence.AnalyticEntry.Companion.STATE_DELIVERY_IN_PROGRESS
import app.cash.paykit.analytics.persistence.sqlite.AnalyticsSQLiteDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class)
class DeliveryWorkerTest {

  @Test
  fun testNoDeliveryHandlers() {
    val dataSource: AnalyticsSQLiteDataSource = mockk(relaxed = true)
    val analyticsOptions: AnalyticsOptions = mockk(relaxed = true)
    val handlers = ArrayList<DeliveryHandler>()
    val worker = DeliveryWorker(dataSource, handlers, AnalyticsLogger(analyticsOptions))
    worker.call()

    verify(inverse = true) { dataSource.generateProcessId(any()) }
    verify(inverse = true) { dataSource.getEntriesForDelivery(any(), any()) }
    verify(inverse = true) { dataSource.updateStatuses(any(), any()) }
    verify(inverse = true) { dataSource.deleteEntry(any()) }
  }

  @Test
  fun testNoDeliverablesToSync() {
    val dataSource: AnalyticsSQLiteDataSource = mockk(relaxed = true)
    val deliveryHandler: DeliveryHandler = mockk(relaxed = true)

    val deliveryType = "TYPE_1"

    every { deliveryHandler.deliverableType } answers { deliveryType }
    every { dataSource.getEntriesForDelivery(any(), any()) } answers {
      Utils.getEntriesToSync(0)
    }

    val handlers = listOf(deliveryHandler)
    val analyticsLogger: AnalyticsLogger = mockk(relaxed = true)
    val worker = DeliveryWorker(dataSource, handlers, analyticsLogger)
    worker.call()

    verify(exactly = 1) { dataSource.generateProcessId(eq(deliveryType)) }
    verify(exactly = 1) { dataSource.getEntriesForDelivery(any(), any()) }
    verify(inverse = true) { dataSource.updateStatuses(any(), any()) }
    verify(inverse = true) { dataSource.deleteEntry(any()) }
  }

  @Test
  fun testWorker() {
    val dataSource: AnalyticsSQLiteDataSource = mockk(relaxed = true)

    // Handler for TYPE_1 entries (sync successful)
    val handler1: DeliveryHandler = mockk(relaxed = true)
    val deliveryType1 = "TYPE_1"

    val deliveryListener1 = object : DeliveryListener {
      override fun onSuccess(entries: List<AnalyticEntry>) {
        dataSource.deleteEntry(entries)
      }

      override fun onError(entries: List<AnalyticEntry>) = Unit
    }
    every { handler1.deliveryListener } answers { deliveryListener1 }
    every { handler1.deliverableType } answers { deliveryType1 }
    every { handler1.deliver(any(), eq(deliveryListener1)) } answers {
      val entries = it.invocation.args[0]
      val listener = it.invocation.args[1] as DeliveryListener
      listener.onSuccess(entries as List<AnalyticEntry>)
    }

    // Handler for TYPE_2 entries (sync failed)
    val handler2: DeliveryHandler = mockk(relaxed = true)
    val deliveryType2 = "TYPE_2"

    val deliveryListener2 = object : DeliveryListener {
      override fun onSuccess(entries: List<AnalyticEntry>) = Unit

      override fun onError(entries: List<AnalyticEntry>) {
        dataSource.updateStatuses(entries, STATE_DELIVERY_FAILED)
      }
    }
    every { handler2.deliveryListener } answers { deliveryListener2 }
    every { handler2.deliverableType } answers { deliveryType2 }
    every { handler2.deliver(any(), eq(deliveryListener2)) } answers {
      val listener = it.invocation.args[1] as DeliveryListener
      listener.onError(args[0] as List<AnalyticEntry>)
    }

    val handlers = listOf(handler1, handler2)

    every { dataSource.getEntriesForDelivery(any(), eq(deliveryType1)) } returns
      Utils.getEntriesToSync(10) andThen
      Utils.getEntriesToSync(5) andThen
      Utils.getEntriesToSync(0)

    every { dataSource.getEntriesForDelivery(any(), eq(deliveryType2)) } returns
      Utils.getEntriesToSync(3) andThen
      Utils.getEntriesToSync(0)

    // start processing
    val analyticsLogger: AnalyticsLogger = mockk(relaxed = true)
    DeliveryWorker(dataSource, handlers, analyticsLogger).call()

    // Processing 1st handler
    verifyOrder {
      dataSource.generateProcessId(eq(deliveryType1))
      dataSource.getEntriesForDelivery(any(), eq(deliveryType1))

      dataSource.updateStatuses(any(), eq(STATE_DELIVERY_IN_PROGRESS))
      dataSource.deleteEntry(any())
      dataSource.getEntriesForDelivery(any(), eq(deliveryType1))

      dataSource.updateStatuses(any(), eq(STATE_DELIVERY_IN_PROGRESS))
      dataSource.deleteEntry(any())
      dataSource.getEntriesForDelivery(any(), eq(deliveryType1))

      // Processing 2nd handler
      dataSource.generateProcessId(eq(deliveryType2))
      dataSource.getEntriesForDelivery(any(), eq(deliveryType2))

      dataSource.updateStatuses(any(), eq(STATE_DELIVERY_IN_PROGRESS))
      dataSource.updateStatuses(any(), eq(STATE_DELIVERY_FAILED))
      dataSource.getEntriesForDelivery(any(), eq(deliveryType2))
    }
  }
}
