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
package app.cash.paykit.core.models.request

import app.cash.paykit.core.fakes.FakeData
import app.cash.paykit.core.models.pii.PiiString
import app.cash.paykit.core.models.sdk.CashAppPayCurrency.USD
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction.OnFileAction
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction.OnFilePayoutAction
import app.cash.paykit.core.models.sdk.CashAppPayPaymentAction.OneTimeAction
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CustomerRequestDataFactoryTests {

  @Test
  fun `build with OnFilePayoutAction returns correct action type`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions).hasSize(1)
    assertThat(result.actions[0].type).isEqualTo("ON_FILE_PAYOUT")
  }

  @Test
  fun `build with OnFilePayoutAction uses scopeId when provided`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].scopeId).isEqualTo(FakeData.BRAND_ID)
  }

  @Test
  fun `build with OnFilePayoutAction defaults to clientId when scopeId is null`() {
    val action = OnFilePayoutAction(scopeId = null)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].scopeId).isEqualTo(FakeData.CLIENT_ID)
  }

  @Test
  fun `build with OnFilePayoutAction wraps accountReferenceId in PiiString`() {
    val accountRef = "customer_123"
    val action = OnFilePayoutAction(
      scopeId = FakeData.BRAND_ID,
      accountReferenceId = accountRef,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].accountReferenceId).isNotNull()
    assertThat(result.actions[0].accountReferenceId).isInstanceOf(PiiString::class.java)
    assertThat(result.actions[0].accountReferenceId.toString()).isEqualTo(accountRef)
  }

  @Test
  fun `build with OnFilePayoutAction sets null accountReferenceId when not provided`() {
    val action = OnFilePayoutAction(
      scopeId = FakeData.BRAND_ID,
      accountReferenceId = null,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].accountReferenceId).isNull()
  }

  @Test
  fun `build with OnFileAction returns correct action type`() {
    val action = OnFileAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions).hasSize(1)
    assertThat(result.actions[0].type).isEqualTo("ON_FILE_PAYMENT")
  }

  @Test
  fun `build with OnFileAction uses scopeId when provided`() {
    val action = OnFileAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].scopeId).isEqualTo(FakeData.BRAND_ID)
  }

  @Test
  fun `build with OnFileAction defaults to clientId when scopeId is null`() {
    val action = OnFileAction(scopeId = null)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].scopeId).isEqualTo(FakeData.CLIENT_ID)
  }

  @Test
  fun `build with OnFileAction wraps accountReferenceId in PiiString`() {
    val accountRef = "customer_456"
    val action = OnFileAction(
      scopeId = FakeData.BRAND_ID,
      accountReferenceId = accountRef,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].accountReferenceId).isNotNull()
    assertThat(result.actions[0].accountReferenceId).isInstanceOf(PiiString::class.java)
    assertThat(result.actions[0].accountReferenceId.toString()).isEqualTo(accountRef)
  }

  @Test
  fun `build with OneTimeAction returns correct action type`() {
    val action = OneTimeAction(
      currency = USD,
      amount = FakeData.FAKE_AMOUNT,
      scopeId = FakeData.BRAND_ID,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions).hasSize(1)
    assertThat(result.actions[0].type).isEqualTo("ONE_TIME_PAYMENT")
  }

  @Test
  fun `build with OneTimeAction includes amount and currency`() {
    val action = OneTimeAction(
      currency = USD,
      amount = FakeData.FAKE_AMOUNT,
      scopeId = FakeData.BRAND_ID,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].amount_cents).isEqualTo(FakeData.FAKE_AMOUNT)
    assertThat(result.actions[0].currency).isEqualTo("USD")
  }

  @Test
  fun `build with OneTimeAction defaults to clientId when scopeId is null`() {
    val action = OneTimeAction(
      currency = USD,
      amount = FakeData.FAKE_AMOUNT,
      scopeId = null,
    )

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
    )

    assertThat(result.actions[0].scopeId).isEqualTo(FakeData.CLIENT_ID)
  }

  @Test
  fun `build with multiple action types preserves order`() {
    val oneTimeAction = OneTimeAction(currency = USD, amount = FakeData.FAKE_AMOUNT)
    val onFileAction = OnFileAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(oneTimeAction, onFileAction),
    )

    assertThat(result.actions).hasSize(2)
    assertThat(result.actions[0].type).isEqualTo("ONE_TIME_PAYMENT")
    assertThat(result.actions[1].type).isEqualTo("ON_FILE_PAYMENT")
  }

  @Test
  fun `build sets channel to IN_APP for new requests`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
      isRequestUpdate = false,
    )

    assertThat(result.channel).isEqualTo("IN_APP")
  }

  @Test
  fun `build sets channel to null for request updates`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
      isRequestUpdate = true,
    )

    assertThat(result.channel).isNull()
  }

  @Test
  fun `build wraps redirectUri in PiiString for new requests`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
      isRequestUpdate = false,
    )

    assertThat(result.redirectUri).isNotNull()
    assertThat(result.redirectUri.toString()).isEqualTo(FakeData.REDIRECT_URI)
  }

  @Test
  fun `build sets redirectUri to null for request updates`() {
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = listOf(action),
      isRequestUpdate = true,
    )

    assertThat(result.redirectUri).isNull()
  }

  @Test
  fun `build wraps referenceId in PiiString when provided`() {
    val refId = "ref_123"
    val action = OnFilePayoutAction(scopeId = FakeData.BRAND_ID)

    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = refId,
      paymentActions = listOf(action),
    )

    assertThat(result.referenceId).isNotNull()
    assertThat(result.referenceId.toString()).isEqualTo(refId)
  }

  @Test
  fun `build with empty action list returns empty actions`() {
    val result = CustomerRequestDataFactory.build(
      clientId = FakeData.CLIENT_ID,
      redirectUri = FakeData.REDIRECT_URI,
      referenceId = null,
      paymentActions = emptyList(),
    )

    assertThat(result.actions).isEmpty()
  }
}
