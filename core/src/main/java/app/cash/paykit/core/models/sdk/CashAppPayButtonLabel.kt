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

/**
 * Call to action shown on the Cash App Pay button in the `ui-views` and `ui-compose` modules.
 *
 * [forActions] picks [Pay], [CashOut], or [Continue] from a customer request. Pass any label directly
 * to override it, for example [Checkout] when the button sits on a checkout screen, or [Buy] for a
 * "Buy now" button.
 */
enum class CashAppPayButtonLabel {
  /** "Pay with Cash App", for payments. */
  Pay,

  /**
   * "Buy with Cash App", for buy-now payments, for example a "Buy now" button on a product page. Never returned by
   * [forActions]; set it explicitly.
   */
  Buy,

  /** "Cash out with Cash App", for payouts. */
  CashOut,

  /** "Checkout with Cash App". Never returned by [forActions]; set it explicitly. */
  Checkout,

  /** "Continue with Cash App", for any other request, such as one that combines payments and payouts. */
  Continue,
  ;

  companion object {
    /**
     * Returns the label for a customer request made of [actions]: [Pay] when every action is a payment,
     * [CashOut] when every action is a payout, and [Continue] otherwise, including when [actions] is empty.
     */
    @JvmStatic
    fun forActions(actions: List<CashAppPayAction>): CashAppPayButtonLabel = when {
      actions.isEmpty() -> Continue
      actions.all { it is CashAppPayAction.OneTimeAction || it is CashAppPayAction.OnFileAction } -> Pay
      actions.all { it is CashAppPayAction.OnFilePayoutAction } -> CashOut
      else -> Continue
    }
  }
}
