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
   * Default style: Polychrome (green) Cash logo on dark background.
   * Recommended for light app backgrounds.
   */
  Default,

  /**
   * Alternative style: Dark monochrome Cash logo on green background.
   * Works equally well in light and dark app backgrounds.
   */
  Alt,

  /**
   * Monochrome dark style: Light Cash logo on dark background.
   * For light app backgrounds.
   */
  MonochromeDark,

  /**
   * Monochrome light style: Dark Cash logo on light outlined background.
   * For dark app backgrounds.
   */
  MonochromeLight,
}
