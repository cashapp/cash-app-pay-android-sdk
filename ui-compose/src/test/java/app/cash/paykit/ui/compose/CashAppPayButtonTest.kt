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
  fun testConnectBlack() = snapshot(CashAppPayButtonLabel.Connect, CashAppPayButtonStyle.Black)

  @Test
  fun testConnectWhite() = snapshot(CashAppPayButtonLabel.Connect, CashAppPayButtonStyle.White)

  @Test
  fun testPayWithBlackDisabled() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.Black, enabled = false)

  @Test
  fun testPayWithWhiteDisabled() = snapshot(CashAppPayButtonLabel.PayWith, CashAppPayButtonStyle.White, enabled = false)

  private fun snapshot(label: CashAppPayButtonLabel, style: CashAppPayButtonStyle, enabled: Boolean = true) {
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
}
