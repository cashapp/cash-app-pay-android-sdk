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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Layout of the [CashAppPaymentMethod].
 */
enum class CashAppPaymentMethodSize {
  /** The cashtag sits under the Cash App logo. */
  Small,

  /** The cashtag sits to the right of the Cash App logo. */
  Large,
}

/**
 * Shows that the customer paid, or will pay, with Cash App: the Cash App logo and the customer's cashtag.
 *
 * The colors follow the system light or dark theme, like the iOS `CashAppPaymentMethod`.
 *
 * @param cashtag The customer's cashtag, for example from `CustomerResponseData.customerProfile`
 * @param modifier Modifier for the view
 * @param size Layout of the view (defaults to [CashAppPaymentMethodSize.Large])
 * @param usePolychromeAsset `true` for the green Cash App logo, `false` (default) for the monochrome logo
 * @param cashtagTextStyle Text style for the cashtag
 * @param cashtagColor Color of the cashtag (defaults to gray in light mode and white in dark mode)
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CashAppPaymentMethod(
  cashtag: String,
  modifier: Modifier = Modifier,
  size: CashAppPaymentMethodSize = CashAppPaymentMethodSize.Large,
  usePolychromeAsset: Boolean = false,
  cashtagTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
  cashtagColor: Color = if (isSystemInDarkTheme()) Color(0xFFFAFAFA) else Color(0xFF999999),
) {
  val isDark = isSystemInDarkTheme()
  val backgroundColor = when {
    usePolychromeAsset && isDark -> Color(0xFF00D64F)
    usePolychromeAsset -> Color.Black
    isDark -> Color.Black
    else -> Color.White
  }
  val logoRes = when {
    usePolychromeAsset && isDark -> R.drawable.cap_compose_logo_cash_app_polychrome_light
    usePolychromeAsset -> R.drawable.cap_compose_logo_cash_app_polychrome_dark
    isDark -> R.drawable.cap_compose_logo_cash_app_monochrome_dark
    else -> R.drawable.cap_compose_logo_cash_app_monochrome_light
  }
  val containerModifier = modifier
    .clip(RoundedCornerShape(8.dp))
    .background(backgroundColor)
  val logo = @Composable {
    Image(
      painter = painterResource(logoRes),
      contentDescription = stringResource(R.string.cap_compose_a11_logo_cash_app),
      modifier = Modifier.height(LOGO_HEIGHT),
    )
  }

  when (size) {
    CashAppPaymentMethodSize.Small -> Column(
      modifier = containerModifier.padding(horizontal = 8.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      logo()
      Text(
        text = cashtag,
        style = cashtagTextStyle,
        color = cashtagColor,
        modifier = Modifier.padding(start = CASHTAG_INSET),
      )
    }

    CashAppPaymentMethodSize.Large -> Row(
      modifier = containerModifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      logo()
      Spacer(modifier = Modifier.weight(1f))
      Text(text = cashtag, style = cashtagTextStyle, color = cashtagColor)
    }
  }
}

// The logo is drawn at the same size as the iOS payment method: a 22.6dp icon tile and wordmark.
private val LOGO_HEIGHT = 22.6.dp

// Lines the cashtag up with the start of the "Cash App" wordmark, after the icon tile.
private val CASHTAG_INSET = 30.dp

@Preview(showBackground = true, backgroundColor = 0xFF555555)
@Composable
private fun CashAppPaymentMethodPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPaymentMethod(cashtag = "\$jack")
    CashAppPaymentMethod(cashtag = "\$jack", size = CashAppPaymentMethodSize.Small)
    CashAppPaymentMethod(cashtag = "\$jack", usePolychromeAsset = true)
  }
}
