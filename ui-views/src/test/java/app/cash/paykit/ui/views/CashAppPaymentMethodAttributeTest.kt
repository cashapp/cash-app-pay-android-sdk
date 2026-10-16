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
 * Checks how the `cap_label` XML attribute maps to [CashAppPaymentMethod.Label].
 */
@RunWith(RobolectricTestRunner::class)
class CashAppPaymentMethodAttributeTest {

  private val activity = Robolectric.buildActivity(Activity::class.java).get()

  @Test
  fun unsetLabelIsCashAppPay() {
    val view = inflate(null)
    assertEquals(CashAppPaymentMethod.Label.CashAppPay, view.label)
    assertEquals("Cash App Pay, \$jack", view.contentDescription)
  }

  @Test
  fun xmlValuesMapToLabels() {
    assertEquals(CashAppPaymentMethod.Label.CashAppPay, inflate("cash_app_pay").label)
    assertEquals(CashAppPaymentMethod.Label.CashApp, inflate("cash_app").label)
    // Button-only values fall back to the default.
    assertEquals(CashAppPaymentMethod.Label.CashAppPay, inflate("pay").label)
  }

  @Test
  fun contentDescriptionMatchesLabel() {
    val view = inflate(null)
    view.label = CashAppPaymentMethod.Label.CashApp
    assertEquals("Cash App, \$jack", view.contentDescription)
  }

  private fun inflate(label: String?): CashAppPaymentMethod {
    val attributes = Robolectric.buildAttributeSet()
    attributes.addAttribute(R.attr.cap_cashtag, "\$jack")
    if (label != null) attributes.addAttribute(R.attr.cap_label, label)
    return CashAppPaymentMethod(activity, attributes.build())
  }
}
