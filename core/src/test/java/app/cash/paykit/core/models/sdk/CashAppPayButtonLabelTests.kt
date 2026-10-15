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
package app.cash.paykit.core.models.sdk

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CashAppPayButtonLabelTests {
  private val oneTime = CashAppPayAction.OneTimeAction(CashAppPayCurrency.USD, 500, "BRAND_1")
  private val onFile = CashAppPayAction.OnFileAction("BRAND_1")
  private val payout = CashAppPayAction.OnFilePayoutAction(scopeId = "BRAND_1")
  private val profile = CashAppPayAction.CustomerProfileSharingAction("BRAND_1")

  @Test
  fun `payments use Pay with`() {
    assertThat(CashAppPayButtonLabel.forActions(listOf(oneTime))).isEqualTo(CashAppPayButtonLabel.Pay)
    assertThat(CashAppPayButtonLabel.forActions(listOf(onFile))).isEqualTo(CashAppPayButtonLabel.Pay)
    assertThat(CashAppPayButtonLabel.forActions(listOf(oneTime, onFile))).isEqualTo(CashAppPayButtonLabel.Pay)
  }

  @Test
  fun `payouts use Cash out with`() {
    assertThat(CashAppPayButtonLabel.forActions(listOf(payout))).isEqualTo(CashAppPayButtonLabel.CashOut)
  }

  @Test
  fun `profile sharing, mixed actions, and no actions use Continue with`() {
    assertThat(CashAppPayButtonLabel.forActions(listOf(profile))).isEqualTo(CashAppPayButtonLabel.Continue)
    assertThat(CashAppPayButtonLabel.forActions(listOf(onFile, payout))).isEqualTo(CashAppPayButtonLabel.Continue)
    assertThat(CashAppPayButtonLabel.forActions(listOf(oneTime, profile))).isEqualTo(CashAppPayButtonLabel.Continue)
    assertThat(CashAppPayButtonLabel.forActions(listOf(payout, profile))).isEqualTo(CashAppPayButtonLabel.Continue)
    assertThat(CashAppPayButtonLabel.forActions(emptyList())).isEqualTo(CashAppPayButtonLabel.Continue)
  }

  @Test
  fun `Checkout with and Buy with are only set explicitly`() {
    val requests = listOf(
      emptyList(),
      listOf(oneTime),
      listOf(onFile),
      listOf(payout),
      listOf(profile),
      listOf(onFile, payout),
      listOf(oneTime, profile),
      listOf(onFile, payout, profile),
    )
    val picked = requests.map { CashAppPayButtonLabel.forActions(it) }.toSet()
    assertThat(picked).containsNoneOf(CashAppPayButtonLabel.Checkout, CashAppPayButtonLabel.Buy)
  }
}
