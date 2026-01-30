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
package app.cash.paykit.core.models.sdk

import app.cash.paykit.core.CashAppPay

/**
 * This class holds the information necessary for [CashAppPay.createCustomerRequest] to be executed.
 */
sealed class CashAppPayPaymentAction(open val scopeId: String?, open val referenceId: String?) {

  /**
   * Describes an intent for a client to charge a customer a given amount.
   *
   * Note the following restrictions when using this action:
   *
   *  - If an amount is provided to the action, the payment charged must exactly equal that amount.
   *  - If no amount is provided to the action, the payment charged may be any amount (use this for tipping support).
   *  - If amount is provided, currency must be provided too (and vice versa).
   *
   * @param currency The type of currency to use for this payment.
   * @param amount Amount for this payment (typically in cents or equivalent monetary unit).
   * @param scopeId ID of the client, brand, or merchant that will charge the customer.
   *
   * If a **client ID** is passed, the grant from this action can be used to create a
   * payment for any merchant owned by the client.
   *
   * If a **brand ID** is passed, the grant from this action can be used to create a
   * payment for any merchant that has a matching brand ID.
   *
   * If a **merchant ID** is passed, the grant from this action can be used to create a
   * payment for the merchant with a matching ID.
   *
   * @param referenceId A user-defined identifier for this request, typically used to
   * associate the resource with a record in an external system.
   */
  data class OneTimeAction(
    val currency: CashAppPayCurrency?,
    val amount: Int?,
    override val scopeId: String? = null,
    override val referenceId: String? = null,
  ) : CashAppPayPaymentAction(scopeId, referenceId)

  /**
   * Describes an intent for a client to store a customer's account, allowing a client to create payments
   * or issue refunds for it on a recurring basis.
   *
   * @param scopeId ID of the client, brand, or merchant that will charge the customer.
   *
   * If a **client ID** is passed, the grant from this action can be used to create a
   * payment for any merchant owned by the client.
   *
   * If a **brand ID** is passed, the grant from this action can be used to create a
   * payment for any merchant that has a matching brand ID.
   *
   * If a **merchant ID** is passed, the grant from this action can be used to create a
   * payment for the merchant with a matching ID.
   *
   * @param accountReferenceId Identifier of the account or customer associated to the on file action.
   * @param referenceId A user-defined identifier for this request, typically used to
   * associate the resource with a record in an external system.
   */
  data class OnFileAction(
    override val scopeId: String? = null,
    val accountReferenceId: String? = null,
    override val referenceId: String? = null,
  ) : CashAppPayPaymentAction(scopeId, referenceId)

  /**
   * Creates a payout to a customer. A payout allows a merchant to send money to a customer’s
   * Cash App account.
   *
   * @param accountReferenceId Identifier of the account or customer associated to the on file action.
   * @param scopeId ID of the client, brand, or merchant that will charge the customer.
   *
   * If a **client ID** is passed, the grant from this action can be used to create a
   * payment for any merchant owned by the client.
   *
   * If a **brand ID** is passed, the grant from this action can be used to create a
   * payment for any merchant that has a matching brand ID.
   *
   * If a **merchant ID** is passed, the grant from this action can be used to create a
   * payment for the merchant with a matching ID.
   *
   * @param referenceId A user-defined identifier for this request, typically used to
   * associate the resource with a record in an external system.
   *
   */
  data class OnFilePayoutAction(
    val accountReferenceId: String? = null,
    override val scopeId: String? = null,
    override val referenceId: String? = null,
  ) : CashAppPayPaymentAction(scopeId, referenceId)
}
