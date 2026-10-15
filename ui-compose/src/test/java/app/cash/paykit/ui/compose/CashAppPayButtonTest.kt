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
package app.cash.paykit.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paykit.core.models.sdk.CashAppPayButtonLabel
import com.android.resources.NightMode
import org.junit.Rule
import org.junit.Test

/**
 * Paparazzi snapshot tests for [CashAppPayButton].
 */
class CashAppPayButtonTest {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5,
    theme = "android:Theme.Material3.Light",
    showSystemUi = false,
  )

  @Test
  fun testPayWithBlack() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Black)

  @Test
  fun testPayWithWhite() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.White)

  @Test
  fun testCashOutWithBlack() = snapshot(CashAppPayButtonLabel.CashOutWith, CashAppPayButtonStyle.Black)

  @Test
  fun testCashOutWithWhite() = snapshot(CashAppPayButtonLabel.CashOutWith, CashAppPayButtonStyle.White)

  @Test
  fun testCheckoutWithBlack() = snapshot(CashAppPayButtonLabel.CheckoutWith, CashAppPayButtonStyle.Black)

  @Test
  fun testCheckoutWithWhite() = snapshot(CashAppPayButtonLabel.CheckoutWith, CashAppPayButtonStyle.White)

  @Test
  fun testContinueWithBlack() = snapshot(CashAppPayButtonLabel.ContinueWith, CashAppPayButtonStyle.Black)

  @Test
  fun testContinueWithWhite() = snapshot(CashAppPayButtonLabel.ContinueWith, CashAppPayButtonStyle.White)

  @Test
  fun testPayWithBlackDisabled() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Black, enabled = false)

  @Test
  fun testPayWithWhiteDisabled() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.White, enabled = false)

  /** Auto shows the black button in light mode. */
  @Test
  fun testPayWithAuto() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Auto)

  private fun snapshot(label: CashAppPayButtonLabel, style: CashAppPayButtonStyle, enabled: Boolean = true) = snapshot(paparazzi, label, style, enabled)
}

/**
 * Paparazzi snapshot tests for [CashAppPayButton] in dark mode.
 */
class CashAppPayButtonDarkModeTest {

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT),
    theme = "android:Theme.Material3.Dark",
    showSystemUi = false,
  )

  /** Auto shows the white button in dark mode. */
  @Test
  fun testPayWithAuto() = snapshot(paparazzi, CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Auto)

  /** Black and White don't follow the UI mode. */
  @Test
  fun testPayWithBlack() = snapshot(paparazzi, CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Black)
}

private fun snapshot(
  paparazzi: Paparazzi,
  label: CashAppPayButtonLabel,
  style: CashAppPayButtonStyle,
  enabled: Boolean = true,
) {
  paparazzi.snapshot {
    Box(modifier = Modifier.padding(16.dp)) {
      CashAppPayButton(
        onClick = {},
        label = label,
        style = style,
        enabled = enabled,
        modifier = Modifier.width(300.dp).height(54.dp),
      )
    }
  }
}
