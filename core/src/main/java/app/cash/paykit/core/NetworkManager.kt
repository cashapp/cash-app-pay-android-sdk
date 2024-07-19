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

import app.cash.paykit.core.models.analytics.EventStream2Response
import app.cash.paykit.core.models.common.NetworkResult
import app.cash.paykit.core.models.response.CustomerTopLevelResponse
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction
import java.io.IOException

internal interface NetworkManager {

  @Throws(IOException::class)
  fun createCustomerRequest(
    clientId: String,
    paymentActions: List<CashAppPayPaymentAction>,
    redirectUri: String?,
    referenceId: String?,
  ): NetworkResult<CustomerTopLevelResponse>

  @Throws(IOException::class)
  fun updateCustomerRequest(
    clientId: String,
    requestId: String,
    referenceId: String?,
    paymentActions: List<CashAppPayPaymentAction>,
  ): NetworkResult<CustomerTopLevelResponse>

  fun retrieveUpdatedRequestData(
    clientId: String,
    requestId: String,
  ): NetworkResult<CustomerTopLevelResponse>

  fun uploadAnalyticsEvents(eventsAsJson: List<String>): NetworkResult<EventStream2Response>
}
