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

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Cash App Pay button composable.
 *
 * A stylized button for initiating Cash App Pay flows. The button handles its own
 * visual styling based on the [style] parameter.
 *
 * **Note**: This button is purely visual. Developers must handle click actions
 * and manage visibility/enabled states based on SDK state.
 *
 * @param onClick Callback invoked when the button is clicked
 * @param modifier Modifier for the button
 * @param style Visual style variant (defaults to [CashAppPayButtonStyle.Default])
 * @param enabled Whether the button is enabled (affects alpha and click handling)
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CashAppPayButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  style: CashAppPayButtonStyle = CashAppPayButtonStyle.Default,
  enabled: Boolean = true,
) {
  val a11yLabel = stringResource(R.string.cap_compose_a11_button_label)
  val shape = RoundedCornerShape(8.dp)

  // Resolve Monochrome style based on app's current UI mode configuration
  val configuration = LocalConfiguration.current
  val isDarkMode =
    (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
  val resolvedStyle = when (style) {
    CashAppPayButtonStyle.Monochrome -> {
      if (isDarkMode) {
        CashAppPayButtonStyle.MonochromeLight
      } else {
        CashAppPayButtonStyle.MonochromeDark
      }
    }

    else -> style
  }

  val buttonProperties = remember(resolvedStyle) {
    when (resolvedStyle) {
      CashAppPayButtonStyle.Default -> ButtonProperties(
        backgroundColor = Color(0xFF101010),
        logoRes = R.drawable.cap_compose_logo_polychrome,
        borderColor = null,
      )

      CashAppPayButtonStyle.Alt -> ButtonProperties(
        backgroundColor = Color(0xFF00D64F),
        logoRes = R.drawable.cap_compose_logo_monochrome_dark,
        borderColor = null,
      )

      CashAppPayButtonStyle.MonochromeDark -> ButtonProperties(
        backgroundColor = Color(0xFF101010),
        logoRes = R.drawable.cap_compose_logo_monochrome_light,
        borderColor = null,
      )

      CashAppPayButtonStyle.MonochromeLight -> ButtonProperties(
        backgroundColor = Color(0xFFFFFFFF),
        logoRes = R.drawable.cap_compose_logo_monochrome_dark,
        borderColor = Color.Black,
      )

      CashAppPayButtonStyle.Monochrome -> error("Monochrome should be resolved before this point")
    }
  }

  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .height(48.dp)
      .fillMaxWidth()
      .alpha(if (enabled) 1f else 0.3f),
    enabled = enabled,
    shape = shape,
    colors = ButtonDefaults.buttonColors(
      containerColor = buttonProperties.backgroundColor,
      disabledContainerColor = buttonProperties.backgroundColor,
    ),
    border = buttonProperties.borderColor?.let { BorderStroke(1.dp, it) },
    contentPadding = ButtonDefaults.ContentPadding,
  ) {
    Image(
      painter = painterResource(buttonProperties.logoRes),
      contentDescription = a11yLabel,
    )
  }
}

private data class ButtonProperties(
  val backgroundColor: Color,
  val logoRes: Int,
  val borderColor: Color?,
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CashAppPayButtonPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Default)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Alt)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeDark)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeLight)
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun CashAppPayButtonDarkBackgroundPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Default)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Alt)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeDark)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeLight)
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CashAppPayButtonDisabledPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Default, enabled = false)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Alt, enabled = false)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeDark, enabled = false)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.MonochromeLight, enabled = false)
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun CashAppPayButtonMonochromeLightThemePreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // In light theme, Monochrome will resolve to MonochromeDark appearance
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Monochrome)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Monochrome, enabled = false)
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CashAppPayButtonMonochromeDarkThemePreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    // In dark theme, Monochrome will resolve to MonochromeLight appearance
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Monochrome)
    CashAppPayButton(onClick = {}, style = CashAppPayButtonStyle.Monochrome, enabled = false)
  }
}
