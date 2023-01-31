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
  )

  fun updatedCustomerRequest(
    requestId: String,
    action: PayKitPaymentAction,
    customerResponseData: CustomerResponseData?,
  )

  fun genericStateChanged(payKitState: PayKitState, customerResponseData: CustomerResponseData?)

  fun stateApproved(customerResponseData: CustomerResponseData)

  fun exceptionOccurred(
    payKitException: PayKitException,
    customerResponseData: CustomerResponseData?,
  )
}
