package com.squareup.cash.paykit

import com.squareup.cash.paykit.models.response.CreateCustomerResponseData

/**
 * Various states Cash App PayKit SDK might be in depending the stage of the transaction process.
 *
 * TODO: Add documentation to each state.
 */
sealed class PayKitState {
  object NotStarted : PayKitState()
  object CreatingCustomerRequest : PayKitState()
  object UpdatingCustomerRequest : PayKitState()
  class ReadyToAuthorize(val responseData: CreateCustomerResponseData) : PayKitState()
  object Authorizing : PayKitState()
  object PollingTransactionStatus : PayKitState()
  class Approved(grants: List<String>?) : PayKitState()
  object Declined : PayKitState()

  class PayKitException(exception: Exception) : PayKitState()
}
