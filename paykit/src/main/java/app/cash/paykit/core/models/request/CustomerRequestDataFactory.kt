package app.cash.paykit.core.models.request

import app.cash.paykit.core.models.common.Action
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OnFileAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OneTimeAction

/**
 * Factory that will create a [CreateCustomerRequest] from a [PayKitPaymentAction].
 */
object CustomerRequestDataFactory {

  private const val CHANNEL_IN_APP = "IN_APP"
  private const val PAYMENT_TYPE_ONE_TIME = "ONE_TIME_PAYMENT"
  private const val PAYMENT_TYPE_ON_FILE = "ON_FILE_PAYMENT"

  fun build(
    clientId: String,
    paymentAction: PayKitPaymentAction,
    isRequestUpdate: Boolean = false,
  ): CustomerRequestData {
    return when (paymentAction) {
      is OnFileAction -> buildFromOnFileAction(clientId, paymentAction, isRequestUpdate)
      is OneTimeAction -> buildFromOneTimeAction(clientId, paymentAction, isRequestUpdate)
    }
  }

  private fun buildFromOnFileAction(
    clientId: String,
    onFileAction: OnFileAction,
    isRequestUpdate: Boolean,
  ): CustomerRequestData {
    // Create request data.
    val scopeIdOrClientId = onFileAction.scopeId ?: clientId
    val requestAction =
      Action(
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ON_FILE,
      )
    return if (isRequestUpdate) {
      CustomerRequestData(
        actions = listOf(requestAction),
        channel = null,
        redirectUri = null,
      )
    } else {
      CustomerRequestData(
        actions = listOf(requestAction),
        channel = CHANNEL_IN_APP,
        redirectUri = onFileAction.redirectUri,
      )
    }
  }

  private fun buildFromOneTimeAction(
    clientId: String,
    oneTimeAction: OneTimeAction,
    isRequestUpdate: Boolean,
  ): CustomerRequestData {
    // Create request data.
    val scopeIdOrClientId = oneTimeAction.scopeId ?: clientId
    val requestAction =
      Action(
        amount_cents = oneTimeAction.amount,
        currency = oneTimeAction.currency?.backendValue,
        scopeId = scopeIdOrClientId,
        type = PAYMENT_TYPE_ONE_TIME,
      )
    return if (isRequestUpdate) {
      CustomerRequestData(
        actions = listOf(requestAction),
        channel = null,
        redirectUri = null,
      )
    } else {
      CustomerRequestData(
        actions = listOf(requestAction),
        channel = CHANNEL_IN_APP,
        redirectUri = oneTimeAction.redirectUri,
      )
    }
  }
}
