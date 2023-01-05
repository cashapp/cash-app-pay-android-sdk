package com.squareup.cash.paykit.models.request

import com.squareup.cash.paykit.models.common.Action
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OnFileAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTimeAction
import java.util.UUID

/**
 * Factory that will create a [CreateCustomerRequest] from a [PayKitPaymentAction].
 */
object CreateCustomerRequestFactory {

  private const val CHANNEL_IN_APP = "IN_APP"
  private const val PAYMENT_TYPE_ONE_TIME = "ONE_TIME_PAYMENT"
  private const val PAYMENT_TYPE_ON_FILE = "ON_FILE_PAYMENT"

  fun build(
    clientId: String,
    paymentAction: PayKitPaymentAction,
  ): CreateCustomerRequest {
    return when (paymentAction) {
      is OnFileAction -> buildFromOnFileAction(clientId, paymentAction)
      is OneTimeAction -> buildFromOneTimeAction(clientId, paymentAction)
    }
  }

  private fun buildFromOnFileAction(
    clientId: String,
    onFileAction: OnFileAction,
  ): CreateCustomerRequest {
    // Create request data.
    val scopeIdOrClientId = onFileAction.scopeId ?: clientId
    val requestAction =
      Action(
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ON_FILE,
      )
    val requestData = CustomerRequestData(
      actions = listOf(requestAction),
      channel = CHANNEL_IN_APP,
      redirectUri = onFileAction.redirectUri,
    )
    return CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = requestData,
    )
  }

  private fun buildFromOneTimeAction(
    clientId: String,
    oneTimeAction: OneTimeAction,
  ): CreateCustomerRequest {
    // Create request data.
    val scopeIdOrClientId = oneTimeAction.scopeId ?: clientId
    val requestAction =
      Action(
        amount_cents = oneTimeAction.amount,
        currency = oneTimeAction.currency?.backendValue,
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ONE_TIME,
      )
    val requestData = CustomerRequestData(
      actions = listOf(requestAction),
      channel = CHANNEL_IN_APP,
      redirectUri = oneTimeAction.redirectUri,
    )
    return CreateCustomerRequest(
      idempotencyKey = UUID.randomUUID().toString(),
      customerRequestData = requestData,
    )
  }
}
