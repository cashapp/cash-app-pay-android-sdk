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
package app.cash.paykit.ui.views

/**
 * Text shown on the [CashAppPayButton], also set with the `cap_label` XML attribute. [CashAppPay] is the default.
 */
enum class CashAppPayButtonLabel {
  /** The Cash App icon and "Cash App". */
  CashApp,

  /** "Pay with Cash App". */
  Pay,

  /** "Buy with Cash App", for example for a "Buy now" button on a product page. */
  Buy,

  /** "Cash out with Cash App". */
  CashOut,

  /** "Checkout with Cash App", for example on a checkout screen. */
  Checkout,

  /** "Continue with Cash App". */
  Continue,

  /** The Cash App icon and "Cash App Pay". The default. */
  CashAppPay,

  /** "Connect to Cash App", for linking the customer's Cash App account, such as saving it for future payments. */
  Connect,
}
