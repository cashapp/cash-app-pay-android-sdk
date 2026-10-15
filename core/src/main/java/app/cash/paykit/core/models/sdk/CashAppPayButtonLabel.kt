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
 * [forActions] picks [PayWith], [CashOutWith], or [ContinueWith] from a customer request. Pass any label directly
 * to override it, for example [CheckoutWith] when the button sits on a checkout screen.
 */
enum class CashAppPayButtonLabel {
  /** "Pay with Cash App", for payments. */
  PayWith,

  /** "Cash out with Cash App", for payouts. */
  CashOutWith,

  /** "Checkout with Cash App". Never returned by [forActions]; set it explicitly. */
  CheckoutWith,

  /** "Continue with Cash App", for any other request, such as one that combines payments and payouts. */
  ContinueWith,
  ;

  companion object {
    /**
     * Returns the label for a customer request made of [actions]: [PayWith] when every action is a payment,
     * [CashOutWith] when every action is a payout, and [ContinueWith] otherwise, including when [actions] is empty.
     */
    @JvmStatic
    fun forActions(actions: List<CashAppPayAction>): CashAppPayButtonLabel = when {
      actions.isEmpty() -> ContinueWith
      actions.all { it is CashAppPayAction.OneTimeAction || it is CashAppPayAction.OnFileAction } -> PayWith
      actions.all { it is CashAppPayAction.OnFilePayoutAction } -> CashOutWith
      else -> ContinueWith
    }
  }
}
