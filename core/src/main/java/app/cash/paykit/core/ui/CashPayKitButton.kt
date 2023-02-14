/*
 * Copyright (C) 2023 Cash App
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

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
  ) {
  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    alpha = if (enabled) {
      1f
    } else {
      .3f
    }
  }
}

/**
 * Cash PayKit button to be used in dark mode. Notice that the button itself is light, as
 * it is meant for contrast with a dark background.
 */
class CashPayKitDarkButton(context: Context, attrs: AttributeSet) :
  CashPayKitButton(
    context,
    attrs,
    R.style.CashPayKitButtonStyle_Dark,
  ) {
  override fun setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    alpha = if (enabled) {
      1f
    } else {
      .4f
    }
  }
}
