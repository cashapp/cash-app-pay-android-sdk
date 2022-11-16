package com.squareup.cash.paykit

/**
 * Various states PayKit SDK might be in depending the stage of the transaction process.
 */
sealed class PayKitState {
  object StateStarted : PayKitState()
  object StateCustomerCreated : PayKitState()
  object StateRequestAuthorization : PayKitState()
  object StatePollingTransactionStatus : PayKitState()
  class StatePendingDeliveryTransactionStatus(val isSuccessful: Boolean) : PayKitState()
  class StateFinished(val isSuccessful: Boolean) : PayKitState()
}
