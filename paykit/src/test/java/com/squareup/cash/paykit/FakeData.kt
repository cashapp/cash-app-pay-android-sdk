package com.squareup.cash.paykit

import com.squareup.cash.paykit.models.sdk.PayKitCurrency.USD
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTimeAction

object FakeData {
  const val CLIENT_ID = "fake_client_id"
  const val BRAND_ID = "fake_brand_id"
  const val REDIRECT_URI = "fake_redirect_uri"
  const val FAKE_AMOUNT = 500

  val oneTimePayment = OneTimeAction(REDIRECT_URI, USD, FAKE_AMOUNT, BRAND_ID)
}
