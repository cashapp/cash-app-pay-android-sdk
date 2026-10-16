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
 * Paparazzi snapshot tests for [CashAppPaymentMethod] in light mode.
 */
class CashAppPaymentMethodTest {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5,
    theme = "android:Theme.Material3.Light",
    showSystemUi = false,
  )

  @Test
  fun testLarge() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = false)

  @Test
  fun testSmall() = snapshot(paparazzi, CashAppPaymentMethodSize.Small, usePolychromeAsset = false)

  @Test
  fun testLargePolychrome() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = true)

  @Test
  fun testLargeCashApp() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = false, CashAppPaymentMethodLabel.CashApp)

  @Test
  fun testSmallCashApp() = snapshot(paparazzi, CashAppPaymentMethodSize.Small, usePolychromeAsset = false, CashAppPaymentMethodLabel.CashApp)

  @Test
  fun testLargePolychromeCashApp() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = true, CashAppPaymentMethodLabel.CashApp)
}

/**
 * Paparazzi snapshot tests for [CashAppPaymentMethod] in dark mode.
 */
class CashAppPaymentMethodDarkModeTest {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5.copy(nightMode = NightMode.NIGHT),
    theme = "android:Theme.Material3.Dark",
    showSystemUi = false,
  )

  @Test
  fun testLarge() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = false)

  @Test
  fun testLargePolychrome() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = true)

  @Test
  fun testLargeCashApp() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = false, CashAppPaymentMethodLabel.CashApp)

  @Test
  fun testLargePolychromeCashApp() = snapshot(paparazzi, CashAppPaymentMethodSize.Large, usePolychromeAsset = true, CashAppPaymentMethodLabel.CashApp)
}

private fun snapshot(
  paparazzi: Paparazzi,
  size: CashAppPaymentMethodSize,
  usePolychromeAsset: Boolean,
  label: CashAppPaymentMethodLabel = CashAppPaymentMethodLabel.CashAppPay,
) {
  paparazzi.snapshot {
    Box(modifier = Modifier.padding(16.dp).width(320.dp)) {
      CashAppPaymentMethod(cashtag = "\$jack", size = size, usePolychromeAsset = usePolychromeAsset, label = label)
    }
  }
}
