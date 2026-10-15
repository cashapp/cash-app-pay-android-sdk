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

/**
 * Visual style variants for the Cash App Pay button.
 */
enum class CashAppPayButtonStyle {
  /**
   * Default style: green Cash App icon and white label on a dark (#101010) background.
   * Recommended for light app backgrounds.
   */
  Default,

  /**
   * Alternative style: dark (#101010) Cash App icon and label on a green (#00D64F) background.
   * Works equally well in light and dark app backgrounds.
   */
  Alt,

  /**
   * Monochrome dark style: white Cash App icon and label on a dark (#101010) background.
   * For light app backgrounds.
   */
  MonochromeDark,

  /**
   * Monochrome light style: black Cash App icon and label on a white background with a dark outline.
   * For dark app backgrounds.
   */
  MonochromeLight,

  /**
   * Theme-aware monochrome style that automatically picks [MonochromeDark] or [MonochromeLight]
   * based on the app's current UI mode configuration.
   * - Light mode: uses the [MonochromeDark] appearance (white icon and label on a dark background)
   * - Dark mode: uses the [MonochromeLight] appearance (black icon and label on a white background)
   */
  Monochrome,

  /**
   * Light style: green Cash App icon and black label on a white background with a dark outline.
   * For dark app backgrounds.
   */
  Light,

  /**
   * Theme-aware style that automatically picks [Default] or [Light] based on the app's current
   * UI mode configuration.
   * - Light mode: uses the [Default] appearance (green icon on a dark background)
   * - Dark mode: uses the [Light] appearance (green icon on a white background)
   */
  Auto,
}
