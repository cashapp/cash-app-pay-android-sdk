package com.squareup.cash.paykit.models.sdk

import com.squareup.cash.paykit.CashAppPayKit

/**
 * This class holds the information necessary for [CashAppPayKit.createCustomerRequest] to be executed.
 */
sealed class PayKitPaymentAction(redirectUri: String, scopeId: String?) {

  /**
   * Describes an intent for a client to charge a customer a given amount.
   *
   * Note the following restrictions when using this action:
   *
   *  - If an amount is provided to the action, the payment charged must exactly equal that amount.
   *  - If no amount is provided to the action, the payment charged may be any amount (use this for tipping support).
   *  - If amount is provided, currency must be provided too (and vice versa).
   *
   * @param redirectUri The URI for Cash App to redirect back to your app.
   * @param currency The type of currency to use for this payment.
   * @param amount Amount for this payment (typically in cents or equivalent monetary unit).
   * @param scopeId This is analogous with the reference ID, an optional field required for brands and merchants support. If null, client ID will be used instead.
   */
  data class OneTimeAction(
    val redirectUri: String,
    val currency: PayKitCurrency,
    val amount: Int?,
    val scopeId: String? = null
  ) : PayKitPaymentAction(redirectUri, scopeId)

  /**
   * Describes an intent for a client to store a customer's account, allowing a client to create payments
   * or issue refunds for it on a recurring basis.
   *
   * @param redirectUri The URI for Cash App to redirect back to your app.
   * @param scopeId This is analogous with the reference ID, an optional field required for brands and merchants support. If null, client ID will be used instead.
   * @param accountReferenceId Identifier of the account or customer associated to the on file action.
   */
  data class OnFileAction(
    val redirectUri: String,
    val scopeId: String? = null,
    val accountReferenceId: String? = null
  ) :
    PayKitPaymentAction(redirectUri, scopeId)
}