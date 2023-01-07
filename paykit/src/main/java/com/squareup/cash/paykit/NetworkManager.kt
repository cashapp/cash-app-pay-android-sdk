package com.squareup.cash.paykit

import com.squareup.cash.paykit.models.common.NetworkResult
import com.squareup.cash.paykit.models.response.CustomerTopLevelResponse
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
import java.io.IOException

internal interface NetworkManager {

  @Throws(IOException::class)
  fun createCustomerRequest(
    clientId: String,
    paymentAction: PayKitPaymentAction,
  ): NetworkResult<CustomerTopLevelResponse>

  @Throws(IOException::class)
  fun updateCustomerRequest(
    clientId: String,
    requestId: String,
    paymentAction: PayKitPaymentAction,
  ): NetworkResult<CustomerTopLevelResponse>

  fun retrieveUpdatedRequestData(
    clientId: String,
    requestId: String,
  ): NetworkResult<CustomerTopLevelResponse>
}
