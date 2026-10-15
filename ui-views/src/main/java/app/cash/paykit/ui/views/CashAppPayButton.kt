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
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageButton
import app.cash.paykit.core.models.sdk.CashAppPayButtonLabel

/**
 * Cash App Pay button. Use it with one of these styles:
 * - `CAPButtonStyle.Black`: white label and green Cash App icon on a black background.
 * - `CAPButtonStyle.White`: black label and green Cash App icon on a white background with a black outline.
 * - `CAPButtonStyle.MonochromeBlack`: white label and icon on a black background.
 * - `CAPButtonStyle.MonochromeWhite`: black label and icon on a white background with a black outline.
 * - `CAPButtonStyle.Green`: black label and icon on a green background.
 * - `CAPButtonStyle.Auto`: follows the system UI mode, `Black` in light mode and `White` in dark mode.
 * - `CAPButtonStyle.MonochromeAuto`: follows the system UI mode, `MonochromeBlack` in light mode and `MonochromeWhite`
 *   in dark mode.
 *
 * Set [label] (or the `cap_label` XML attribute) to match the customer request, for example with
 * [CashAppPayButtonLabel.forActions], or set another label, such as [CashAppPayButtonLabel.BuyWith] or
 * [CashAppPayButtonLabel.CheckoutWith], to override it.
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

  private val variant: Variant

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
      variant = Variant.entries[attributes.getInt(R.styleable.CashAppPayButton_cap_variant, 0)]
      imageTintList = variant.tint?.let { ColorStateList.valueOf(it) }
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
      when (variant.art) {
        LabelArt.Black -> when (label) {
          CashAppPayButtonLabel.PayWith -> R.drawable.cap_label_pay_with_black
          CashAppPayButtonLabel.BuyWith -> R.drawable.cap_label_buy_with_black
          CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_label_cash_out_with_black
          CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_label_checkout_with_black
          CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_label_continue_with_black
        }

        LabelArt.White -> when (label) {
          CashAppPayButtonLabel.PayWith -> R.drawable.cap_label_pay_with_white
          CashAppPayButtonLabel.BuyWith -> R.drawable.cap_label_buy_with_white
          CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_label_cash_out_with_white
          CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_label_checkout_with_white
          CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_label_continue_with_white
        }

        LabelArt.Mono -> when (label) {
          CashAppPayButtonLabel.PayWith -> R.drawable.cap_label_pay_with_mono
          CashAppPayButtonLabel.BuyWith -> R.drawable.cap_label_buy_with_mono
          CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_label_cash_out_with_mono
          CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_label_checkout_with_mono
          CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_label_continue_with_mono
        }
      },
    )
    contentDescription = context.getString(
      when (label) {
        CashAppPayButtonLabel.PayWith -> R.string.cap_a11_button_label_pay_with
        CashAppPayButtonLabel.BuyWith -> R.string.cap_a11_button_label_buy_with
        CashAppPayButtonLabel.CashOutWith -> R.string.cap_a11_button_label_cash_out_with
        CashAppPayButtonLabel.CheckoutWith -> R.string.cap_a11_button_label_checkout_with
        CashAppPayButtonLabel.ContinueWith -> R.string.cap_a11_button_label_continue_with
      },
    )
  }

  /** Label artwork: color art for black or white backgrounds, or solid monochrome art meant to be tinted. */
  private enum class LabelArt { Black, White, Mono }

  /** Values of the `cap_variant` XML enum, in the same order. [tint] is null for the color art. */
  private enum class Variant(val art: LabelArt, val tint: Int?) {
    Black(LabelArt.Black, null),
    White(LabelArt.White, null),
    MonochromeBlack(LabelArt.Mono, Color.WHITE),
    MonochromeWhite(LabelArt.Mono, Color.BLACK),
    Green(LabelArt.Mono, 0xFF101010.toInt()),
  }
}
