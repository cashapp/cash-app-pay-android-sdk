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
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

/**
 * Shows that the customer paid, or will pay, with Cash App: the "Cash App Pay" (default) or "Cash App" logo and the
 * customer's cashtag.
 *
 * The colors follow the system light or dark theme, like the iOS `CashAppPaymentMethod`. Set [cashtag], [size],
 * [usePolychromeAsset], and [logo] in code, or `app:cap_cashtag`, `app:cap_size`, `app:cap_use_polychrome_asset`, and
 * `app:cap_logo` (`cash_app_pay` or `cash_app`) in XML.
 */
class CashAppPaymentMethod @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

  /** Layout of the view. */
  enum class Size {
    /** The cashtag sits under the logo. */
    Small,

    /** The cashtag sits to the right of the logo. */
    Large,
  }

  /** Logo shown by the view. */
  enum class Logo {
    /** The Cash App icon and "Cash App Pay", matching the iOS `CashAppPaymentMethod`. The default. */
    CashAppPay,

    /** The Cash App icon and "Cash App". */
    CashApp,
  }

  private val logoView = ImageView(context).apply {
    adjustViewBounds = true
    // The container announces the logo and the cashtag together, as one accessibility node.
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
  }
  private val cashtagView = TextView(context).apply {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    // Matches the Compose cashtag's line height, which is closer to iOS.
    includeFontPadding = false
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
  }
  private val background = GradientDrawable().apply { cornerRadius = dp(8f) }
  private var cashtagTextColorOverride: Int? = null

  /** The customer's cashtag, for example from `CustomerResponseData.customerProfile?.cashTag`. */
  var cashtag: CharSequence
    get() = cashtagView.text
    set(value) {
      cashtagView.text = value
      updateContentDescription()
    }

  /** Layout of the view. Defaults to [Size.Large]. */
  var size: Size = Size.Large
    set(value) {
      field = value
      updateLayout()
    }

  /**
   * `true` for the polychrome logo, `false` (default) for the monochrome logo. With [Logo.CashAppPay], the polychrome
   * logo is the green Cash App icon on a black background in light mode, and the black icon on a green background in
   * dark mode. With [Logo.CashApp], it is the green Cash App icon on a black background in light and dark mode.
   */
  var usePolychromeAsset: Boolean = false
    set(value) {
      field = value
      updateColors()
    }

  /** Logo to show. Defaults to [Logo.CashAppPay]. */
  var logo: Logo = Logo.CashAppPay
    set(value) {
      field = value
      updateColors()
      updateContentDescription()
    }

  init {
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    setBackground(background)
    addView(logoView, LayoutParams(LayoutParams.WRAP_CONTENT, dp(LOGO_HEIGHT_DP).roundToInt()))
    addView(cashtagView)

    val attributes = context.obtainStyledAttributes(attrs, R.styleable.CashAppPaymentMethod, defStyleAttr, 0)
    try {
      cashtag = attributes.getString(R.styleable.CashAppPaymentMethod_cap_cashtag).orEmpty()
      size = Size.entries[attributes.getInt(R.styleable.CashAppPaymentMethod_cap_size, Size.Large.ordinal)]
      usePolychromeAsset = attributes.getBoolean(R.styleable.CashAppPaymentMethod_cap_use_polychrome_asset, false)
      logo = Logo.entries[attributes.getInt(R.styleable.CashAppPaymentMethod_cap_logo, Logo.CashAppPay.ordinal)]
    } finally {
      attributes.recycle()
    }
    updateLayout()
    updateColors()
  }

  /** Overrides the cashtag color, which defaults to gray in light mode and white in dark mode. */
  fun setCashtagTextColor(color: Int) {
    cashtagTextColorOverride = color
    updateColors()
  }

  private fun updateContentDescription() {
    val logoDescription = when (logo) {
      Logo.CashAppPay -> R.string.cap_a11_logo_cash_app_pay
      Logo.CashApp -> R.string.cap_a11_logo_cash_app
    }
    contentDescription = listOf(context.getString(logoDescription), cashtag)
      .filter { it.isNotBlank() }
      .joinToString(separator = ", ")
  }

  private fun updateLayout() {
    val cashtagParams = cashtagView.layoutParams as LayoutParams
    val logoParams = logoView.layoutParams as LayoutParams
    val horizontal = dp(8f).roundToInt()
    val vertical = dp(12f).roundToInt()
    // Like iOS, the logo takes up a LOGO_SLOT_HEIGHT_DP row and is drawn slightly taller, overflowing it evenly.
    val logoOverflow = (logoParams.height - dp(LOGO_SLOT_HEIGHT_DP).roundToInt()) / 2
    when (size) {
      Size.Small -> {
        setPadding(horizontal, vertical, horizontal, vertical)
        logoParams.topMargin = -logoOverflow
        logoParams.bottomMargin = -logoOverflow
        orientation = VERTICAL
        gravity = Gravity.START
        cashtagParams.width = LayoutParams.WRAP_CONTENT
        cashtagParams.weight = 0f
        cashtagParams.topMargin = dp(2f).toInt()
        cashtagParams.marginStart = dp(CASHTAG_INSET_DP).toInt()
        cashtagView.gravity = Gravity.START
        logoView.translationY = dp(SMALL_LOGO_OFFSET_DP)
      }

      Size.Large -> {
        // As on iOS, only the logo has vertical padding, so the logo row alone sets the height.
        setPadding(horizontal, 0, horizontal, 0)
        logoParams.topMargin = vertical - logoOverflow
        logoParams.bottomMargin = vertical - logoOverflow
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        cashtagParams.width = 0
        cashtagParams.weight = 1f
        cashtagParams.topMargin = 0
        cashtagParams.marginStart = 0
        cashtagView.gravity = Gravity.END
        logoView.translationY = 0f
      }
    }
    cashtagView.layoutParams = cashtagParams
    logoView.layoutParams = logoParams
  }

  private fun updateColors() {
    val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    when (logo) {
      Logo.CashAppPay -> {
        background.setColor(
          when {
            usePolychromeAsset && isDark -> CASH_APP_GREEN
            usePolychromeAsset || isDark -> Color.BLACK
            else -> Color.WHITE
          },
        )
        logoView.setImageResource(
          when {
            usePolychromeAsset && isDark -> R.drawable.cap_logo_cash_app_pay_polychrome_dark
            usePolychromeAsset -> R.drawable.cap_logo_cash_app_pay_polychrome
            isDark -> R.drawable.cap_logo_cash_app_pay_monochrome_dark
            else -> R.drawable.cap_logo_cash_app_pay_monochrome_light
          },
        )
      }

      Logo.CashApp -> {
        background.setColor(if (usePolychromeAsset || isDark) Color.BLACK else Color.WHITE)
        logoView.setImageResource(
          when {
            usePolychromeAsset -> R.drawable.cap_logo_cash_app_polychrome
            isDark -> R.drawable.cap_logo_cash_app_monochrome_dark
            else -> R.drawable.cap_logo_cash_app_monochrome_light
          },
        )
      }
    }
    val defaultCashtagColor = if (isDark) Color.rgb(0xFA, 0xFA, 0xFA) else Color.rgb(0x99, 0x99, 0x99)
    cashtagView.setTextColor(cashtagTextColorOverride ?: defaultCashtagColor)
  }

  private fun dp(value: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

// Both logos are drawn at the same size as the iOS payment method, which draws the "Cash App Pay" and "Cash App"
// logos at the same height: a 22.6dp icon tile and wordmark.
private const val LOGO_HEIGHT_DP = 22.6f

// The height the logo takes up in the layout, as on iOS.
private const val LOGO_SLOT_HEIGHT_DP = 20f

// In the small layout, iOS draws the logo 2dp lower without moving the cashtag.
private const val SMALL_LOGO_OFFSET_DP = 2f

// Lines the cashtag up with the start of the "Cash App Pay" or "Cash App" wordmark, after the icon tile.
private const val CASHTAG_INSET_DP = 30f

private val CASH_APP_GREEN = Color.rgb(0x00, 0xD6, 0x4F)
