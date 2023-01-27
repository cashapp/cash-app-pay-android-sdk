package app.cash.paykit.core.analytics

import app.cash.paykit.core.PayKitState
import app.cash.paykit.core.PayKitState.PayKitException
import app.cash.paykit.core.models.response.CustomerResponseData
import app.cash.paykit.core.models.sdk.PayKitPaymentAction

/**
 * Definition of analytics events that we want to capture.
 */
internal interface PayKitAnalyticsEvents {

  fun sdkInitialized()

  fun eventListenerAdded()

  fun eventListenerRemoved()

  fun createdCustomerRequest(
    action: PayKitPaymentAction,
    channel: String,
    redirectUrl: String,
    referenceId: String?,
  )

  fun genericStateChanged(payKitState: PayKitState, customerResponseData: CustomerResponseData)

  fun exceptionOccurred(payKitException: PayKitException)
}
