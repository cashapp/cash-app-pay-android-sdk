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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.cash.paykit.core.models.sdk.CashAppPayButtonLabel

/**
 * Cash App Pay button composable.
 *
 * A stylized button for starting a Cash App Pay flow. Pick the [label] that matches the customer request,
 * for example with [CashAppPayButtonLabel.forActions], or pass [CashAppPayButtonLabel.CheckoutWith] or
 * [CashAppPayButtonLabel.ContinueWith] to override it.
 *
 * **Note**: This button is purely visual. Developers must handle click actions
 * and manage visibility/enabled states based on SDK state.
 *
 * @param onClick Callback invoked when the button is clicked
 * @param label Call to action shown on the button
 * @param modifier Modifier for the button
 * @param style Visual style variant (defaults to [CashAppPayButtonStyle.Black]). [CashAppPayButtonStyle.Auto] follows
 *   the system UI mode.
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
  val isWhite = when (style) {
    CashAppPayButtonStyle.Black -> false
    CashAppPayButtonStyle.White -> true
    CashAppPayButtonStyle.Auto -> isSystemInDarkTheme()
  }
  val backgroundColor = if (isWhite) Color.White else Color(0xFF101010)

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
    contentPadding = ButtonDefaults.ContentPadding,
  ) {
    Image(
      painter = painterResource(labelDrawable(label, isWhite)),
      contentDescription = stringResource(labelText(label)),
    )
  }
}

private fun labelDrawable(label: CashAppPayButtonLabel, isWhite: Boolean): Int = if (isWhite) {
  when (label) {
    CashAppPayButtonLabel.PayWith -> R.drawable.cap_compose_label_pay_with_white
    CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_compose_label_cash_out_with_white
    CashAppPayButtonLabel.Connect -> R.drawable.cap_compose_label_connect_white
    CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_compose_label_checkout_with_white
    CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_compose_label_continue_with_white
  }
} else {
  when (label) {
    CashAppPayButtonLabel.PayWith -> R.drawable.cap_compose_label_pay_with_black
    CashAppPayButtonLabel.CashOutWith -> R.drawable.cap_compose_label_cash_out_with_black
    CashAppPayButtonLabel.Connect -> R.drawable.cap_compose_label_connect_black
    CashAppPayButtonLabel.CheckoutWith -> R.drawable.cap_compose_label_checkout_with_black
    CashAppPayButtonLabel.ContinueWith -> R.drawable.cap_compose_label_continue_with_black
  }
}

private fun labelText(label: CashAppPayButtonLabel): Int = when (label) {
  CashAppPayButtonLabel.PayWith -> R.string.cap_compose_a11_button_label_pay_with
  CashAppPayButtonLabel.CashOutWith -> R.string.cap_compose_a11_button_label_cash_out_with
  CashAppPayButtonLabel.Connect -> R.string.cap_compose_a11_button_label_connect
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
