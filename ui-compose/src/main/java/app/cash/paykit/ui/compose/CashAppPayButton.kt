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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.cash.paykit.core.models.sdk.CashAppPayButtonLabel

/**
 * Cash App Pay button composable.
 *
 * A stylized button for starting a Cash App Pay flow. Pick the [label] that matches the customer request,
 * for example with [CashAppPayButtonLabel.forActions], or pass another label, such as [CashAppPayButtonLabel.BuyWith]
 * or [CashAppPayButtonLabel.CheckoutWith], to override it.
 *
 * **Note**: This button is purely visual. Developers must handle click actions
 * and manage visibility/enabled states based on SDK state.
 *
 * @param onClick Callback invoked when the button is clicked
 * @param label Call to action shown on the button
 * @param modifier Modifier for the button
 * @param style Visual style variant (defaults to [CashAppPayButtonStyle.Black]). [CashAppPayButtonStyle.Auto] and
 *   [CashAppPayButtonStyle.MonochromeAuto] follow the system UI mode.
 * @param enabled Whether the button is enabled (affects alpha and click handling)
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CashAppPayButton(
  onClick: () -> Unit,
  label: CashAppPayButtonLabel,
  modifier: Modifier = Modifier,
  style: CashAppPayButtonStyle = CashAppPayButtonStyle.Black,
  enabled: Boolean = true,
) {
  val darkTheme = isSystemInDarkTheme()
  val resolved = when (style) {
    CashAppPayButtonStyle.Auto -> if (darkTheme) CashAppPayButtonStyle.White else CashAppPayButtonStyle.Black

    CashAppPayButtonStyle.MonochromeAuto ->
      if (darkTheme) CashAppPayButtonStyle.MonochromeWhite else CashAppPayButtonStyle.MonochromeBlack

    else -> style
  }
  val backgroundColor = when (resolved) {
    CashAppPayButtonStyle.White, CashAppPayButtonStyle.MonochromeWhite -> Color.White
    CashAppPayButtonStyle.Green -> CashAppGreen
    else -> CashAppBlack
  }
  val border = when (resolved) {
    CashAppPayButtonStyle.White, CashAppPayButtonStyle.MonochromeWhite -> BorderStroke(1.dp, CashAppBlack)
    else -> null
  }
  val tint = when (resolved) {
    CashAppPayButtonStyle.MonochromeBlack -> Color.White
    CashAppPayButtonStyle.MonochromeWhite -> Color.Black
    CashAppPayButtonStyle.Green -> CashAppBlack
    else -> null
  }
  val art = when (resolved) {
    CashAppPayButtonStyle.White -> LabelArt.White
    CashAppPayButtonStyle.Black -> LabelArt.Black
    else -> LabelArt.Mono
  }

  Button(
    onClick = onClick,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .height(48.dp)
      .fillMaxWidth()
      .alpha(if (enabled) 1f else 0.3f),
    enabled = enabled,
    shape = RoundedCornerShape(8.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = backgroundColor,
      disabledContainerColor = backgroundColor,
    ),
    border = border,
    contentPadding = ButtonDefaults.ContentPadding,
  ) {
    Image(
      painter = painterResource(labelDrawable(label, art)),
      contentDescription = stringResource(labelText(label)),
      colorFilter = tint?.let { ColorFilter.tint(it) },
    )
  }
}

private val CashAppBlack = Color(0xFF101010)
private val CashAppGreen = Color(0xFF00D64F)

/** Label artwork: color art for black or white backgrounds, or solid monochrome art meant to be tinted. */
private enum class LabelArt { Black, White, Mono }

private fun labelDrawable(label: CashAppPayButtonLabel, art: LabelArt): Int = when (art) {
  LabelArt.Black -> when (label) {
    CashAppPayButtonLabel.PayWith -> R.drawable.cap_compose_label_pay_with_black
    CashAppPayButtonLabel.BuyWith -> R.drawable.cap_compose_label_buy_with_black
    CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_compose_label_cash_out_with_black
    CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_compose_label_checkout_with_black
    CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_compose_label_continue_with_black
  }

  LabelArt.White -> when (label) {
    CashAppPayButtonLabel.PayWith -> R.drawable.cap_compose_label_pay_with_white
    CashAppPayButtonLabel.BuyWith -> R.drawable.cap_compose_label_buy_with_white
    CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_compose_label_cash_out_with_white
    CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_compose_label_checkout_with_white
    CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_compose_label_continue_with_white
  }

  LabelArt.Mono -> when (label) {
    CashAppPayButtonLabel.PayWith -> R.drawable.cap_compose_label_pay_with_mono
    CashAppPayButtonLabel.BuyWith -> R.drawable.cap_compose_label_buy_with_mono
    CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_compose_label_cash_out_with_mono
    CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_compose_label_checkout_with_mono
    CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_compose_label_continue_with_mono
  }
}

private fun labelText(label: CashAppPayButtonLabel): Int = when (label) {
  CashAppPayButtonLabel.PayWith -> R.string.cap_compose_a11_button_label_pay_with
  CashAppPayButtonLabel.BuyWith -> R.string.cap_compose_a11_button_label_buy_with
  CashAppPayButtonLabel.CashOutWith -> R.string.cap_compose_a11_button_label_cash_out_with
  CashAppPayButtonLabel.CheckoutWith -> R.string.cap_compose_a11_button_label_checkout_with
  CashAppPayButtonLabel.ContinueWith -> R.string.cap_compose_a11_button_label_continue_with
}

@Preview(showBackground = true, backgroundColor = 0xFFE8E8E8)
@Composable
private fun CashAppPayButtonPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPayButtonStyle.entries.forEach { style ->
      CashAppPayButtonLabel.entries.forEach { label ->
        CashAppPayButton(onClick = {}, label = label, style = style)
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFE8E8E8)
@Composable
private fun CashAppPayButtonDisabledPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPayButtonStyle.entries.forEach { style ->
      CashAppPayButton(onClick = {}, label = CashAppPayButtonLabel.PayWith, style = style, enabled = false)
    }
  }
}
