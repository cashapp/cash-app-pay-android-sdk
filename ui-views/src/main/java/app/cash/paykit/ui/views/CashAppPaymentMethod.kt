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

/**
 * Shows that the customer paid, or will pay, with Cash App: the Cash App logo and the customer's cashtag.
 *
 * The colors follow the system light or dark theme, like the iOS `CashAppPaymentMethod`. Set [cashtag], [size],
 * and [usePolychromeAsset] in code, or `app:cap_cashtag`, `app:cap_size`, and `app:cap_use_polychrome_asset` in XML.
 */
class CashAppPaymentMethod @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

  /** Layout of the view. */
  enum class Size {
    /** The cashtag sits under the Cash App logo. */
    Small,

    /** The cashtag sits to the right of the Cash App logo. */
    Large,
  }

  private val logoView = ImageView(context).apply {
    adjustViewBounds = true
    contentDescription = context.getString(R.string.cap_a11_logo_cash_app)
  }
  private val cashtagView = TextView(context).apply {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
  }
  private val background = GradientDrawable().apply { cornerRadius = dp(8f) }

  /** The customer's cashtag, for example from `CustomerResponseData.customerProfile`. */
  var cashtag: CharSequence
    get() = cashtagView.text
    set(value) {
      cashtagView.text = value
    }

  /** Layout of the view. Defaults to [Size.Large]. */
  var size: Size = Size.Large
    set(value) {
      field = value
      updateLayout()
    }

  /** `true` for the green Cash App icon on a black background in light and dark mode, `false` (default) for the monochrome logo. */
  var usePolychromeAsset: Boolean = false
    set(value) {
      field = value
      updateColors()
    }

  init {
    setBackground(background)
    val horizontal = dp(8f).toInt()
    val vertical = dp(12f).toInt()
    setPadding(horizontal, vertical, horizontal, vertical)
    addView(logoView, LayoutParams(LayoutParams.WRAP_CONTENT, dp(LOGO_HEIGHT_DP).toInt()))
    addView(cashtagView)

    val attributes = context.obtainStyledAttributes(attrs, R.styleable.CashAppPaymentMethod, defStyleAttr, 0)
    try {
      cashtagView.text = attributes.getString(R.styleable.CashAppPaymentMethod_cap_cashtag).orEmpty()
      size = Size.entries[attributes.getInt(R.styleable.CashAppPaymentMethod_cap_size, Size.Large.ordinal)]
      usePolychromeAsset = attributes.getBoolean(R.styleable.CashAppPaymentMethod_cap_use_polychrome_asset, false)
    } finally {
      attributes.recycle()
    }
    updateLayout()
    updateColors()
  }

  /** Overrides the cashtag color, which defaults to gray in light mode and white in dark mode. */
  fun setCashtagTextColor(color: Int) {
    cashtagView.setTextColor(color)
  }

  private fun updateLayout() {
    val cashtagParams = cashtagView.layoutParams as LayoutParams
    when (size) {
      Size.Small -> {
        orientation = VERTICAL
        gravity = Gravity.START
        cashtagParams.width = LayoutParams.WRAP_CONTENT
        cashtagParams.weight = 0f
        cashtagParams.topMargin = dp(2f).toInt()
        cashtagParams.marginStart = dp(CASHTAG_INSET_DP).toInt()
        cashtagView.gravity = Gravity.START
      }

      Size.Large -> {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        cashtagParams.width = 0
        cashtagParams.weight = 1f
        cashtagParams.topMargin = 0
        cashtagParams.marginStart = 0
        cashtagView.gravity = Gravity.END
      }
    }
    cashtagView.layoutParams = cashtagParams
  }

  private fun updateColors() {
    val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    background.setColor(if (usePolychromeAsset || isDark) Color.BLACK else Color.WHITE)
    logoView.setImageResource(
      when {
        usePolychromeAsset -> R.drawable.cap_logo_cash_app_polychrome
        isDark -> R.drawable.cap_logo_cash_app_monochrome_dark
        else -> R.drawable.cap_logo_cash_app_monochrome_light
      },
    )
    cashtagView.setTextColor(if (isDark) Color.rgb(0xFA, 0xFA, 0xFA) else Color.rgb(0x99, 0x99, 0x99))
  }

  private fun dp(value: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}

// The logo is drawn at the same size as the iOS payment method: a 22.6dp icon tile and wordmark.
private const val LOGO_HEIGHT_DP = 22.6f

// Lines the cashtag up with the start of the "Cash App" wordmark, after the icon tile.
private const val CASHTAG_INSET_DP = 30f
