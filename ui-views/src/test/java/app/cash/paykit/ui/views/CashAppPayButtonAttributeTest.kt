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
package app.cash.paykit.ui.views

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Checks how the `cap_label` XML attribute maps to [CashAppPayButtonLabel].
 */
@RunWith(RobolectricTestRunner::class)
class CashAppPayButtonAttributeTest {

  private val activity = Robolectric.buildActivity(Activity::class.java).get()

  @Test
  fun unsetLabelIsCashAppPay() {
    assertEquals(CashAppPayButtonLabel.CashAppPay, inflate(null).label)
  }

  @Test
  fun xmlValuesMapToLabels() {
    val expected = mapOf(
      "cash_app" to CashAppPayButtonLabel.CashApp,
      "pay" to CashAppPayButtonLabel.Pay,
      "buy" to CashAppPayButtonLabel.Buy,
      "cash_out" to CashAppPayButtonLabel.CashOut,
      "checkout" to CashAppPayButtonLabel.Checkout,
      "continue_with" to CashAppPayButtonLabel.Continue,
      "cash_app_pay" to CashAppPayButtonLabel.CashAppPay,
    )
    expected.forEach { (xmlValue, label) ->
      assertEquals(xmlValue, label, inflate(xmlValue).label)
    }
  }

  private fun inflate(label: String?): CashAppPayButton {
    val attributes = Robolectric.buildAttributeSet()
    if (label != null) attributes.addAttribute(R.attr.cap_label, label)
    return CashAppPayButton(activity, attributes.build())
  }
}
