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
import app.cash.paykit.ui.compose.CashAppPayButtonTest.Companion.BUTTON_MODIFIER
import app.cash.paykit.ui.compose.CashAppPayButtonTest.Companion.CONTAINER_MODIFIER
import app.cash.paykit.ui.compose.CashAppPayButtonTest.Companion.DARK_THEME
import app.cash.paykit.ui.compose.CashAppPayButtonTest.Companion.DEVICE_CONFIG
import com.android.resources.NightMode
import org.junit.Rule
import org.junit.Test

/**
 * Paparazzi snapshot tests for [CashAppPayButton].
 *
 * These tests verify visual appearance of all button style variants, disabled states,
 * and theme-aware Monochrome style resolution.
 */
class CashAppPayButtonTest {

  companion object {
    const val LIGHT_THEME = "android:Theme.Material3.Light"
    const val DARK_THEME = "android:Theme.Material3.Dark"
    val DEVICE_CONFIG = DeviceConfig.PIXEL_5
    val BUTTON_WIDTH = 300.dp
    val BUTTON_HEIGHT = 54.dp
    val CONTAINER_PADDING = 16.dp
    val CONTAINER_MODIFIER = Modifier.padding(CONTAINER_PADDING)
    val BUTTON_MODIFIER = Modifier
      .width(BUTTON_WIDTH)
      .height(BUTTON_HEIGHT)
  }

  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DEVICE_CONFIG,
    theme = LIGHT_THEME,
    showSystemUi = false,
  )

  // Style Variant Tests (Enabled State)

  @Test
  fun testDefaultStyle() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Default,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testAltStyle() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Alt,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeDarkStyle() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.MonochromeDark,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeLightStyle() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.MonochromeLight,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeStyleInLightMode() {
    // In light mode, Monochrome should resolve to MonochromeDark appearance
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Monochrome,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  // Disabled State Tests

  @Test
  fun testDefaultStyleDisabled() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Default,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testAltStyleDisabled() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Alt,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeDarkStyleDisabled() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.MonochromeDark,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeLightStyleDisabled() {
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.MonochromeLight,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeStyleInLightModeDisabled() {
    // In light mode, disabled Monochrome should resolve to MonochromeDark appearance with translucency
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Monochrome,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }
}

/**
 * Dark theme snapshot tests for [CashAppPayButton].
 *
 * Verifies theme-aware Monochrome style resolution in dark mode.
 */
class CashAppPayButtonDarkModeTest {
  @get:Rule
  val paparazzi = Paparazzi(
    deviceConfig = DEVICE_CONFIG.copy(nightMode = NightMode.NIGHT),
    theme = DARK_THEME,
    showSystemUi = false,
  )

  @Test
  fun testMonochromeStyleInDarkMode() {
    // In dark mode, Monochrome should resolve to MonochromeLight appearance
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Monochrome,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }

  @Test
  fun testMonochromeStyleInDarkModeDisabled() {
    // In dark mode, disabled Monochrome should resolve to MonochromeLight appearance with 0.3 alpha
    paparazzi.snapshot {
      Box(modifier = CONTAINER_MODIFIER) {
        CashAppPayButton(
          onClick = {},
          style = CashAppPayButtonStyle.Monochrome,
          enabled = false,
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }
}
