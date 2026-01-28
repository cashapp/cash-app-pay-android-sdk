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
package app.cash.paykit.core

import app.cash.paykit.analytics.PayKitAnalytics
import app.cash.paykit.core.analytics.AnalyticsEventStream2Event
import app.cash.paykit.core.analytics.PayKitAnalyticsEventDispatcherImpl
import app.cash.paykit.core.fakes.FakeClock
import app.cash.paykit.core.fakes.FakeData
import app.cash.paykit.core.fakes.FakeUUIDManager
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class PayKitAnalyticsEventDispatcherImplTest {

  @MockK(relaxed = true)
  private lateinit var networkManager: NetworkManager

  @MockK(relaxed = true)
  private lateinit var paykitAnalytics: PayKitAnalytics

  @Before
  fun setup() {
    MockKAnnotations.init(this)
  }

  @Test
  fun `sdkInitialized records valid analytics event`() {
    val analyticsDispatcher = buildDispatcher()
    analyticsDispatcher.sdkInitialized()
    val contents =
      """{"app_name":"paykitsdk-android","catalog_name":"mobile_cap_pk_initialization","json_data":"{\"mobile_cap_pk_initialization_sdk_version\":\"1.0.0\",\"mobile_cap_pk_initialization_client_ua\":\"Webkit/1.0.0 (Linux; U; Android 12; en-US; Samsung Build/XYZ)\",\"mobile_cap_pk_initialization_platform\":\"android\",\"mobile_cap_pk_initialization_client_id\":\"fake_client_id\",\"mobile_cap_pk_initialization_environment\":\"SANDBOX\"}","recorded_at_usec":123,"uuid":"abc"}"""

    verify { paykitAnalytics.scheduleForDelivery(AnalyticsEventStream2Event(contents)) }
  }

  @Test
  fun `eventListenerAdded records valid analytics event`() {
    val analyticsDispatcher = buildDispatcher()
    analyticsDispatcher.eventListenerAdded()
    val contents =
      """{"app_name":"paykitsdk-android","catalog_name":"mobile_cap_pk_event_listener","json_data":"{\"mobile_cap_pk_event_listener_sdk_version\":\"1.0.0\",\"mobile_cap_pk_event_listener_client_ua\":\"Webkit/1.0.0 (Linux; U; Android 12; en-US; Samsung Build/XYZ)\",\"mobile_cap_pk_event_listener_platform\":\"android\",\"mobile_cap_pk_event_listener_client_id\":\"fake_client_id\",\"mobile_cap_pk_event_listener_environment\":\"SANDBOX\",\"mobile_cap_pk_event_listener_is_added\":true}","recorded_at_usec":123,"uuid":"abc"}"""

    verify { paykitAnalytics.scheduleForDelivery(AnalyticsEventStream2Event(contents)) }
  }

  @Test
  fun `eventListenerRemoved records valid analytics event`() {
    val analyticsDispatcher = buildDispatcher()
    analyticsDispatcher.eventListenerRemoved()
    val contents = """{"app_name":"paykitsdk-android","catalog_name":"mobile_cap_pk_event_listener","json_data":"{\"mobile_cap_pk_event_listener_sdk_version\":\"1.0.0\",\"mobile_cap_pk_event_listener_client_ua\":\"Webkit/1.0.0 (Linux; U; Android 12; en-US; Samsung Build/XYZ)\",\"mobile_cap_pk_event_listener_platform\":\"android\",\"mobile_cap_pk_event_listener_client_id\":\"fake_client_id\",\"mobile_cap_pk_event_listener_environment\":\"SANDBOX\",\"mobile_cap_pk_event_listener_is_added\":false}","recorded_at_usec":123,"uuid":"abc"}"""

    verify { paykitAnalytics.scheduleForDelivery(AnalyticsEventStream2Event(contents)) }
  }

  @Test
  fun `SDK state update records valid analytics event`() {
    val analyticsDispatcher = buildDispatcher()
    analyticsDispatcher.genericStateChanged(CashAppPayState.Authorizing, null)
    val contents = """{"app_name":"paykitsdk-android","catalog_name":"mobile_cap_pk_customer_request","json_data":"{\"mobile_cap_pk_customer_request_sdk_version\":\"1.0.0\",\"mobile_cap_pk_customer_request_client_ua\":\"Webkit/1.0.0 (Linux; U; Android 12; en-US; Samsung Build/XYZ)\",\"mobile_cap_pk_customer_request_platform\":\"android\",\"mobile_cap_pk_customer_request_client_id\":\"fake_client_id\",\"mobile_cap_pk_customer_request_environment\":\"SANDBOX\",\"mobile_cap_pk_customer_request_action\":\"redirect\",\"mobile_cap_pk_customer_request_channel\":\"IN_APP\"}","recorded_at_usec":123,"uuid":"abc"}"""

    verify { paykitAnalytics.scheduleForDelivery(AnalyticsEventStream2Event(contents)) }
  }

  private fun buildDispatcher() = PayKitAnalyticsEventDispatcherImpl(
    FakeData.SDK_VERSION,
    FakeData.CLIENT_ID,
    FakeData.USER_AGENT,
    FakeData.SDK_ENVIRONMENT_SANDBOX,
    paykitAnalytics,
    networkManager,
    uuidManager = FakeUUIDManager(),
    clock = FakeClock(),
  )
}
