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
package app.cash.paykit.core.analytics

import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.PayKitState.Approved
import app.cash.paykit.core.PayKitState.PayKitExceptionState
import app.cash.paykit.core.models.common.Action
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction

/**
 * Definition of analytics events that we want to capture.
 */
internal interface PayKitAnalyticsEventDispatcher {

  fun sdkInitialized()

  fun eventListenerAdded()

  fun eventListenerRemoved()

  fun createdCustomerRequest(
    paymentKitAction: PayKitPaymentAction,
    apiAction: Action,
  )

  fun updatedCustomerRequest(
    requestId: String,
    paymentKitAction: PayKitPaymentAction,
    apiAction: Action,
  )

  fun genericStateChanged(payKitState: PayKitState, customerResponseData: CustomerResponseData?)

  fun stateApproved(approved: Approved)

  fun exceptionOccurred(
    payKitExceptionState: PayKitExceptionState,
    customerResponseData: CustomerResponseData?,
  )

  /**
   * Command this [PayKitAnalyticsEventDispatcher] to stop executing and discard.
   */
  fun shutdown()
}
