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

  /** No label shows the default, [CashAppPayButtonLabel.CashAppPay]. */
  @Test
  fun testDefaultLabel() = paparazzi.snapshot {
    Box(modifier = Modifier.padding(16.dp)) {
      CashAppPayButton(onClick = {}, modifier = Modifier.width(300.dp).height(54.dp))
    }
  }

  /** Calls the button positionally the way 3.0.0 callers do, so the 3.0.0 source API keeps compiling. */
  @Test
  fun testPositionalCall() = paparazzi.snapshot {
    Box(modifier = Modifier.padding(16.dp)) {
      CashAppPayButton({}, Modifier.width(300.dp).height(54.dp), CashAppPayButtonStyle.MonochromeDark, true)
    }
  }

  @Test
  fun testCashAppPayDefault() = snapshot(CashAppPayButtonLabel.CashAppPay, CashAppPayButtonStyle.Default)

  @Test
  fun testCashAppPayLight() = snapshot(CashAppPayButtonLabel.CashAppPay, CashAppPayButtonStyle.Light)

  @Test
  fun testCashAppPayMonochromeDark() = snapshot(CashAppPayButtonLabel.CashAppPay, CashAppPayButtonStyle.MonochromeDark)

  @Test
  fun testCashAppPayMonochromeLight() = snapshot(CashAppPayButtonLabel.CashAppPay, CashAppPayButtonStyle.MonochromeLight)

  @Test
  fun testCashAppPayAlt() = snapshot(CashAppPayButtonLabel.CashAppPay, CashAppPayButtonStyle.Alt)

  @Test
  fun testCashAppDefault() = snapshot(CashAppPayButtonLabel.CashApp, CashAppPayButtonStyle.Default)

  @Test
  fun testCashAppLight() = snapshot(CashAppPayButtonLabel.CashApp, CashAppPayButtonStyle.Light)

  @Test
  fun testCashAppMonochromeDark() = snapshot(CashAppPayButtonLabel.CashApp, CashAppPayButtonStyle.MonochromeDark)

  @Test
  fun testCashAppMonochromeLight() = snapshot(CashAppPayButtonLabel.CashApp, CashAppPayButtonStyle.MonochromeLight)

  @Test
  fun testCashAppAlt() = snapshot(CashAppPayButtonLabel.CashApp, CashAppPayButtonStyle.Alt)

  @Test
  fun testPayWithDefault() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Default)

  @Test
  fun testPayWithLight() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Light)

  @Test
  fun testBuyWithDefault() = snapshot(CashAppPayButtonLabel.Buy, CashAppPayButtonStyle.Default)

  @Test
  fun testBuyWithLight() = snapshot(CashAppPayButtonLabel.Buy, CashAppPayButtonStyle.Light)

  @Test
  fun testCashOutWithDefault() = snapshot(CashAppPayButtonLabel.CashOut, CashAppPayButtonStyle.Default)

  @Test
  fun testCashOutWithLight() = snapshot(CashAppPayButtonLabel.CashOut, CashAppPayButtonStyle.Light)

  @Test
  fun testCheckoutWithDefault() = snapshot(CashAppPayButtonLabel.Checkout, CashAppPayButtonStyle.Default)

  @Test
  fun testCheckoutWithLight() = snapshot(CashAppPayButtonLabel.Checkout, CashAppPayButtonStyle.Light)

  @Test
  fun testContinueWithDefault() = snapshot(CashAppPayButtonLabel.Continue, CashAppPayButtonStyle.Default)

  @Test
  fun testContinueWithLight() = snapshot(CashAppPayButtonLabel.Continue, CashAppPayButtonStyle.Light)

  @Test
  fun testPayWithMonochromeDark() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.MonochromeDark)

  @Test
  fun testPayWithMonochromeLight() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.MonochromeLight)

  @Test
  fun testPayWithAlt() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Alt)

  @Test
  fun testPayWithDefaultDisabled() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Default, enabled = false)

  @Test
  fun testPayWithLightDisabled() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Light, enabled = false)

  /** Auto shows [CashAppPayButtonStyle.Default] in light mode. */
  @Test
  fun testPayWithAuto() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Auto)

  /** Monochrome shows [CashAppPayButtonStyle.MonochromeDark] in light mode. */
  @Test
  fun testPayWithMonochrome() = snapshot(CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Monochrome)

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

  /** Auto shows [CashAppPayButtonStyle.Light] in dark mode. */
  @Test
  fun testPayWithAuto() = snapshot(paparazzi, CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Auto)

  /** Monochrome shows [CashAppPayButtonStyle.MonochromeLight] in dark mode. */
  @Test
  fun testPayWithMonochrome() = snapshot(paparazzi, CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Monochrome)

  /** Default doesn't follow the UI mode. */
  @Test
  fun testPayWithDefault() = snapshot(paparazzi, CashAppPayButtonLabel.Pay, CashAppPayButtonStyle.Default)
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
