package app.cash.paykit.core

import app.cash.paykit.core.models.common.NetworkResult
import app.cash.paykit.core.models.response.CustomerTopLevelResponse
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
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
