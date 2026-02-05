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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
  val interactionSource = remember { MutableInteractionSource() }
  val shape = RoundedCornerShape(8.dp)

  val buttonProperties = remember(style) {
    when (style) {
      CashAppPayButtonStyle.Default -> ButtonProperties(
        backgroundColor = Color(0xFF101010),
        logoRes = R.drawable.cap_compose_logo_polychrome,
        hasBorder = false,
        rippleColor = Color.White,
      )

      CashAppPayButtonStyle.Alt -> ButtonProperties(
        backgroundColor = Color(0xFF00D64F),
        logoRes = R.drawable.cap_compose_logo_monochrome_dark,
        hasBorder = false,
        rippleColor = Color.Black,
      )

      CashAppPayButtonStyle.MonochromeDark -> ButtonProperties(
        backgroundColor = Color(0xFF101010),
        logoRes = R.drawable.cap_compose_logo_monochrome_light,
        hasBorder = false,
        rippleColor = Color.White,
      )

      CashAppPayButtonStyle.MonochromeLight -> ButtonProperties(
        backgroundColor = Color(0xFFFFFFFF),
        logoRes = R.drawable.cap_compose_logo_monochrome_dark,
        hasBorder = true,
        rippleColor = Color.Black,
      )
    }
  }

  val rippleIndication = ripple(color = buttonProperties.rippleColor)

  Box(
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .height(48.dp)
      .fillMaxWidth()
      .alpha(if (enabled) 1f else 0.3f)
      .clip(shape)
      .background(buttonProperties.backgroundColor)
      .then(
        if (buttonProperties.hasBorder) Modifier.border(1.dp, Color.Black, shape) else Modifier,
      )
      .clickable(
        enabled = enabled,
        interactionSource = interactionSource,
        indication = rippleIndication,
        onClick = onClick,
      )
      .semantics { contentDescription = a11yLabel },
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(buttonProperties.logoRes),
      contentDescription = null,
    )
  }
}

private data class ButtonProperties(
  val backgroundColor: Color,
  val logoRes: Int,
  val hasBorder: Boolean,
  val rippleColor: Color,
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
