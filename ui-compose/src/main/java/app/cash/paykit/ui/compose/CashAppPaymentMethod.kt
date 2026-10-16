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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Layout of the [CashAppPaymentMethod].
 */
enum class CashAppPaymentMethodSize {
  /** The cashtag sits under the logo. */
  Small,

  /** The cashtag sits to the right of the logo. */
  Large,
}

/**
 * Label shown by the [CashAppPaymentMethod], drawn as a logo.
 */
enum class CashAppPaymentMethodLabel {
  /** The Cash App icon and "Cash App Pay", matching the iOS `CashAppPaymentMethod`. The default. */
  CashAppPay,

  /** The Cash App icon and "Cash App". */
  CashApp,
}

/**
 * Shows that the customer paid, or will pay, with Cash App: the "Cash App Pay" (default) or "Cash App" logo and the
 * customer's cashtag.
 *
 * The colors follow the system light or dark theme, like the iOS `CashAppPaymentMethod`.
 *
 * @param cashtag The customer's cashtag, for example from `CustomerResponseData.customerProfile?.cashTag`
 * @param modifier Modifier for the view
 * @param size Layout of the view (defaults to [CashAppPaymentMethodSize.Large])
 * @param usePolychromeAsset `true` for the polychrome logo, `false` (default) for the monochrome logo. With
 *   [CashAppPaymentMethodLabel.CashAppPay], the polychrome logo is the green Cash App icon on a black background in
 *   light mode, and the black icon on a green background in dark mode. With [CashAppPaymentMethodLabel.CashApp], it is
 *   the green Cash App icon on a black background in light and dark mode.
 * @param cashtagTextStyle Text style for the cashtag
 * @param cashtagColor Color of the cashtag (defaults to gray in light mode and white in dark mode, or near-black on the
 *   green [CashAppPaymentMethodLabel.CashAppPay] polychrome background in dark mode)
 * @param label Label to show (defaults to [CashAppPaymentMethodLabel.CashAppPay])
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun CashAppPaymentMethod(
  cashtag: String,
  modifier: Modifier = Modifier,
  size: CashAppPaymentMethodSize = CashAppPaymentMethodSize.Large,
  usePolychromeAsset: Boolean = false,
  cashtagTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
  cashtagColor: Color = if (isSystemInDarkTheme()) CashtagColorDark else CashtagColorLight,
  label: CashAppPaymentMethodLabel = CashAppPaymentMethodLabel.CashAppPay,
) {
  val isDark = isSystemInDarkTheme()
  val backgroundColor: Color
  val logoRes: Int
  val logoDescription: Int
  when (label) {
    CashAppPaymentMethodLabel.CashAppPay -> {
      backgroundColor = when {
        usePolychromeAsset && isDark -> CashAppGreen
        usePolychromeAsset || isDark -> Color.Black
        else -> Color.White
      }
      logoRes = when {
        usePolychromeAsset && isDark -> R.drawable.cap_compose_logo_cash_app_pay_polychrome_dark
        usePolychromeAsset -> R.drawable.cap_compose_logo_cash_app_pay_polychrome
        isDark -> R.drawable.cap_compose_logo_cash_app_pay_monochrome_dark
        else -> R.drawable.cap_compose_logo_cash_app_pay_monochrome_light
      }
      logoDescription = R.string.cap_compose_a11_logo_cash_app_pay
    }

    CashAppPaymentMethodLabel.CashApp -> {
      backgroundColor = if (usePolychromeAsset || isDark) Color.Black else Color.White
      logoRes = when {
        usePolychromeAsset -> R.drawable.cap_compose_logo_cash_app_polychrome
        isDark -> R.drawable.cap_compose_logo_cash_app_monochrome_dark
        else -> R.drawable.cap_compose_logo_cash_app_monochrome_light
      }
      logoDescription = R.string.cap_compose_a11_logo_cash_app
    }
  }
  // Like the logo's wordmark, the default cashtag is near-black on the green background. A custom color is used as set.
  val isOnGreen = label == CashAppPaymentMethodLabel.CashAppPay && usePolychromeAsset && isDark
  val resolvedCashtagColor = if (isOnGreen && cashtagColor == CashtagColorDark) CashtagColorOnGreen else cashtagColor
  val containerModifier = modifier
    .clip(RoundedCornerShape(8.dp))
    .background(backgroundColor)
    // TalkBack reads the logo and the cashtag together, as one focus target.
    .semantics(mergeDescendants = true) {}
  // Like iOS, the logo takes up a LOGO_SLOT_HEIGHT row and is drawn slightly taller, overflowing it evenly.
  val logoImage = @Composable { slotModifier: Modifier ->
    Box(modifier = slotModifier.height(LOGO_SLOT_HEIGHT), contentAlignment = Alignment.CenterStart) {
      Image(
        painter = painterResource(logoRes),
        contentDescription = stringResource(logoDescription),
        modifier = Modifier.requiredHeight(LOGO_HEIGHT),
      )
    }
  }

  when (size) {
    CashAppPaymentMethodSize.Small -> Column(
      modifier = containerModifier.padding(horizontal = 8.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      logoImage(Modifier.offset(y = SMALL_LOGO_OFFSET))
      Text(
        text = cashtag,
        style = cashtagTextStyle,
        color = resolvedCashtagColor,
        modifier = Modifier.padding(start = CASHTAG_INSET),
      )
    }

    CashAppPaymentMethodSize.Large -> Row(
      modifier = containerModifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // As on iOS, only the logo has vertical padding, so the logo row alone sets the height.
      logoImage(Modifier.padding(vertical = 12.dp))
      Spacer(modifier = Modifier.weight(1f))
      Text(text = cashtag, style = cashtagTextStyle, color = resolvedCashtagColor)
    }
  }
}

/**
 * The 3.2.0 signature of [CashAppPaymentMethod], kept so code compiled against 3.2.0 keeps linking.
 */
@Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
@Suppress("ktlint:standard:function-naming")
@Composable
fun CashAppPaymentMethod(
  cashtag: String,
  modifier: Modifier = Modifier,
  size: CashAppPaymentMethodSize = CashAppPaymentMethodSize.Large,
  usePolychromeAsset: Boolean = false,
  cashtagTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
  cashtagColor: Color = if (isSystemInDarkTheme()) CashtagColorDark else CashtagColorLight,
) {
  CashAppPaymentMethod(
    cashtag = cashtag,
    modifier = modifier,
    size = size,
    usePolychromeAsset = usePolychromeAsset,
    cashtagTextStyle = cashtagTextStyle,
    cashtagColor = cashtagColor,
    label = CashAppPaymentMethodLabel.CashAppPay,
  )
}

private val CashAppGreen = Color(0xFF00D64F)
private val CashtagColorLight = Color(0xFF999999)
private val CashtagColorDark = Color(0xFFFAFAFA)
private val CashtagColorOnGreen = Color(0xFF101010)

// Both logos are drawn at the same size as the iOS payment method, which draws the "Cash App Pay" and "Cash App"
// logos at the same height: a 22.6dp icon tile and wordmark.
private val LOGO_HEIGHT = 22.6.dp

// The height the logo takes up in the layout, as on iOS.
private val LOGO_SLOT_HEIGHT = 20.dp

// In the small layout, iOS draws the logo 2dp lower without moving the cashtag.
private val SMALL_LOGO_OFFSET = 2.dp

// Lines the cashtag up with the start of the "Cash App Pay" or "Cash App" wordmark, after the icon tile.
private val CASHTAG_INSET = 30.dp

@Preview(showBackground = true, backgroundColor = 0xFF555555)
@Composable
private fun CashAppPaymentMethodPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    CashAppPaymentMethodLabel.entries.forEach { label ->
      CashAppPaymentMethod(cashtag = "\$jack", label = label)
      CashAppPaymentMethod(cashtag = "\$jack", size = CashAppPaymentMethodSize.Small, label = label)
      CashAppPaymentMethod(cashtag = "\$jack", usePolychromeAsset = true, label = label)
    }
  }
}
