package app.cash.paykit.core.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import app.cash.paykit.core.R

abstract class CashPayKitButton(context: Context, attrs: AttributeSet, style: Int) :
  ImageButton(
    context,
    attrs,
    0,
    style,
  )

/**
 * Cash PayKit button to be used in light mode. Notice that the button itself is dark, as
 * it is meant for contrast with a light background.
 */
class CashPayKitLightButton(context: Context, attrs: AttributeSet) :
  CashPayKitButton(
    context,
    attrs,
    R.style.CashPayKitButtonStyle_Light,
  )

/**
 * Cash PayKit button to be used in dark mode. Notice that the button itself is light, as
 * it is meant for contrast with a dark background.
 */
class CashPayKitDarkButton(context: Context, attrs: AttributeSet) :
  CashPayKitButton(
    context,
    attrs,
    R.style.CashPayKitButtonStyle_Dark,
  )
