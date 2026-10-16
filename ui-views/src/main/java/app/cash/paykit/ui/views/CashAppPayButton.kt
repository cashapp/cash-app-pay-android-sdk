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

/**
 * Cash App Pay button. Use it with one of these styles, or none for the default, `CAPButtonStyle.Default`:
 * - `CAPButtonStyle.Default` (default): white label and green Cash App icon on a dark background.
 * - `CAPButtonStyle.Alt`: dark label and icon on a green background.
 * - `CAPButtonStyle.MonochromeDark`: white label and icon on a dark background.
 * - `CAPButtonStyle.MonochromeLight`: black label and icon on a white background with a dark outline.
 * - `CAPButtonStyle.Light`: black label and green Cash App icon on a white background with a dark outline.
 * - `CAPButtonStyle.Auto`: follows the system UI mode, `Default` in light mode and `Light` in dark mode.
 * - `CAPButtonStyle.Monochrome`: follows the system UI mode, `MonochromeDark` in light mode and `MonochromeLight` in
 *   dark mode.
 *
 * By default the button shows the Cash App icon and "Cash App Pay". Set [label] (or the `cap_label` XML attribute) to
 * show a call to action instead, such as [CashAppPayButtonLabel.Pay] or [CashAppPayButtonLabel.Checkout].
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
  defaultStyleRes(style),
) {

  private val variant: Variant

  /**
   * Text shown on the button. Defaults to [CashAppPayButtonLabel.CashAppPay].
   */
  var label: CashAppPayButtonLabel = CashAppPayButtonLabel.CashAppPay
    set(value) {
      field = value
      updateLabel()
    }

  init {
    val attributes = context.obtainStyledAttributes(attrs, R.styleable.CashAppPayButton, 0, defaultStyleRes(style))
    try {
      variant = Variant.entries[attributes.getInt(R.styleable.CashAppPayButton_cap_variant, 0)]
      imageTintList = variant.tint?.let { ColorStateList.valueOf(it) }
      // The cap_label XML enum values are the CashAppPayButtonLabel ordinals.
      val labelOrdinal = attributes.getInt(R.styleable.CashAppPayButton_cap_label, CashAppPayButtonLabel.CashAppPay.ordinal)
      label = CashAppPayButtonLabel.entries[labelOrdinal]
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
          CashAppPayButtonLabel.CashApp -> R.drawable.cap_label_cash_app_black
          CashAppPayButtonLabel.Pay -> R.drawable.cap_label_pay_with_black
          CashAppPayButtonLabel.Buy -> R.drawable.cap_label_buy_with_black
          CashAppPayButtonLabel.CashOut -> R.drawable.cap_label_cash_out_with_black
          CashAppPayButtonLabel.Checkout -> R.drawable.cap_label_checkout_with_black
          CashAppPayButtonLabel.Continue -> R.drawable.cap_label_continue_with_black
          CashAppPayButtonLabel.CashAppPay -> R.drawable.cap_label_cash_app_pay_black
        }

        LabelArt.White -> when (label) {
          CashAppPayButtonLabel.CashApp -> R.drawable.cap_label_cash_app_white
          CashAppPayButtonLabel.Pay -> R.drawable.cap_label_pay_with_white
          CashAppPayButtonLabel.Buy -> R.drawable.cap_label_buy_with_white
          CashAppPayButtonLabel.CashOut -> R.drawable.cap_label_cash_out_with_white
          CashAppPayButtonLabel.Checkout -> R.drawable.cap_label_checkout_with_white
          CashAppPayButtonLabel.Continue -> R.drawable.cap_label_continue_with_white
          CashAppPayButtonLabel.CashAppPay -> R.drawable.cap_label_cash_app_pay_white
        }

        LabelArt.Mono -> when (label) {
          CashAppPayButtonLabel.CashApp -> R.drawable.cap_label_cash_app_mono
          CashAppPayButtonLabel.Pay -> R.drawable.cap_label_pay_with_mono
          CashAppPayButtonLabel.Buy -> R.drawable.cap_label_buy_with_mono
          CashAppPayButtonLabel.CashOut -> R.drawable.cap_label_cash_out_with_mono
          CashAppPayButtonLabel.Checkout -> R.drawable.cap_label_checkout_with_mono
          CashAppPayButtonLabel.Continue -> R.drawable.cap_label_continue_with_mono
          CashAppPayButtonLabel.CashAppPay -> R.drawable.cap_label_cash_app_pay_mono
        }
      },
    )
    contentDescription = context.getString(
      when (label) {
        CashAppPayButtonLabel.CashApp -> R.string.cap_a11_logo_cash_app
        CashAppPayButtonLabel.Pay -> R.string.cap_a11_button_label_pay_with
        CashAppPayButtonLabel.Buy -> R.string.cap_a11_button_label_buy_with
        CashAppPayButtonLabel.CashOut -> R.string.cap_a11_button_label_cash_out_with
        CashAppPayButtonLabel.Checkout -> R.string.cap_a11_button_label_checkout_with
        CashAppPayButtonLabel.Continue -> R.string.cap_a11_button_label_continue_with
        CashAppPayButtonLabel.CashAppPay -> R.string.cap_a11_logo_cash_app_pay
      },
    )
  }

  /** Label artwork: color art for black or white backgrounds, or solid monochrome art meant to be tinted. */
  private enum class LabelArt { Black, White, Mono }

  /** Values of the `cap_variant` XML enum, in the same order. [tint] is null for the color art. */
  private enum class Variant(val art: LabelArt, val tint: Int?) {
    Default(LabelArt.Black, null),
    Alt(LabelArt.Mono, 0xFF101010.toInt()),
    MonochromeDark(LabelArt.Mono, Color.WHITE),
    MonochromeLight(LabelArt.Mono, Color.BLACK),
    Light(LabelArt.White, null),
  }
}

/**
 * Falls back to `CAPButtonStyle.Default` when no style resource is given. The default style resource has the lowest
 * precedence, so a `style` set in XML (which arrives through the attribute set) still wins over it.
 */
internal fun defaultStyleRes(style: Int): Int = if (style == 0) R.style.CAPButtonStyle_Default else style
