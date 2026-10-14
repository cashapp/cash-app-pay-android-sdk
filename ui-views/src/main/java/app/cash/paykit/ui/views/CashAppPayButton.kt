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

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import app.cash.paykit.core.models.sdk.CashAppPayButtonLabel

/**
 * Cash App Pay button. Use it with either the `CAPButtonStyle.Black` or `CAPButtonStyle.White` style,
 * and set [label] (or the `cap_label` XML attribute) to match the customer request, for example with
 * [CashAppPayButtonLabel.forActions]. Set [CashAppPayButtonLabel.CheckoutWith] or [CashAppPayButtonLabel.ContinueWith]
 * to override it.
 *
 * **Note**: Due to its unmanaged nature, the button is merely a stylized button, it's up to developers
 * to trigger the correct action on button press, as well as manage any visibility states of the
 * button accordingly.
 */
class CashAppPayButton @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  style: Int = 0,
) : ImageButton(
  context,
  attrs,
  0,
  style,
) {

  private val isWhite: Boolean

  /**
   * Call to action shown on the button. Defaults to [CashAppPayButtonLabel.PayWith].
   */
  var label: CashAppPayButtonLabel = CashAppPayButtonLabel.PayWith
    set(value) {
      field = value
      updateLabel()
    }

  init {
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.CashAppPayButton, 0, style)
    try {
      isWhite = attributes.getInt(R.styleable.CashAppPayButton_cap_variant, 0) == 1
      label = CashAppPayButtonLabel.entries[attributes.getInt(R.styleable.CashAppPayButton_cap_label, 0)]
    } finally {
      attributes.recycle()
    }
  }

  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    alpha = if (enabled) {
      1f
    } else {
      .3f
    }
  }

  private fun updateLabel() {
    setImageResource(
      when (label) {
        CashAppPayButtonLabel.PayWith -> if (isWhite) R.drawable.cap_label_pay_with_white else R.drawable.cap_label_pay_with_black
        CashAppPayButtonLabel.CashOutWith -> if (isWhite) R.drawable.cap_label_cash_out_with_white else R.drawable.cap_label_cash_out_with_black
        CashAppPayButtonLabel.Connect -> if (isWhite) R.drawable.cap_label_connect_white else R.drawable.cap_label_connect_black
        CashAppPayButtonLabel.CheckoutWith -> if (isWhite) R.drawable.cap_label_checkout_with_white else R.drawable.cap_label_checkout_with_black
        CashAppPayButtonLabel.ContinueWith -> if (isWhite) R.drawable.cap_label_continue_with_white else R.drawable.cap_label_continue_with_black
      },
    )
    contentDescription = context.getString(
      when (label) {
        CashAppPayButtonLabel.PayWith -> R.string.cap_a11_button_label_pay_with
        CashAppPayButtonLabel.CashOutWith -> R.string.cap_a11_button_label_cash_out_with
        CashAppPayButtonLabel.Connect -> R.string.cap_a11_button_label_connect
        CashAppPayButtonLabel.CheckoutWith -> R.string.cap_a11_button_label_checkout_with
        CashAppPayButtonLabel.ContinueWith -> R.string.cap_a11_button_label_continue_with
      },
    )
  }
}
