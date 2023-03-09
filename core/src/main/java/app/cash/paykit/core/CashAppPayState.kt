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

import app.cash.paykit.core.models.response.CustomerResponseData

/**
 * Various states Cash App Pay SDK might be in depending the stage of the transaction process.
 *
 */
sealed interface CashAppPayState {

  /**
   * This is the initial Cash App Pay State.
   */
  object NotStarted : CashAppPayState

  /**
   * This state denotes that a the Create Customer Request has been started. If successful, this state
   * will transition to [ReadyToAuthorize].
   */
  object CreatingCustomerRequest : CashAppPayState

  /**
   * This state denotes that a the Update Customer Request has been started. If successful, this state
   * will transition to [ReadyToAuthorize].
   */
  object UpdatingCustomerRequest : CashAppPayState

  /**
   * This state denotes that the SDK is in the process of retrieving an existing Customer Request.
   * If successful, this state will transition into the corresponding state of the request that was
   * retrieved, which can be one of the following: [ReadyToAuthorize], [Approved], [Declined].
   */
  object RetrievingExistingCustomerRequest : CashAppPayState

  /**
   * This state denotes that a valid Customer Request exists, and we're ready to authorize upon
   * user action.
   */
  class ReadyToAuthorize(val responseData: CustomerResponseData) : CashAppPayState

  /**
   * This state denotes that we've entered the process of authorizing an existing customer request.
   * This state will transition to [PollingTransactionStatus].
   */
  object Authorizing : CashAppPayState

  /**
   * This state denotes that we're actively polling for an authorization update. This state will
   * typically transition to either [Approved] or [Declined].
   */
  object PollingTransactionStatus : CashAppPayState

  /**
   * Terminal state denoting that the request authorization was approved.
   */
  class Approved(val responseData: CustomerResponseData) : CashAppPayState

  /**
   * Terminal state denoting that the request authorization was declined.
   */
  object Declined : CashAppPayState

  /**
   * Terminal state that can happen as a result of most states, indicates that an exception has
   * occurred.
   * This state is typically unrecoverable, and it is advised to restart the process from scratch in
   * case it is met.
   */
  class CashAppPayExceptionState(val exception: Exception) : CashAppPayState
}
