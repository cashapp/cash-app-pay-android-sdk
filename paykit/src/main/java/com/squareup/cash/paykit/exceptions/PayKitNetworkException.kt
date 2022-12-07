package com.squareup.cash.paykit.exceptions

/**
 * This exception represents a network related issue. Subclasses of this will be used to represent higher granularity.
 * See [PayKitNetworkErrorType] for more.
 */
open class PayKitNetworkException(val errorType: PayKitNetworkErrorType) : Exception()

enum class PayKitNetworkErrorType {
  API,
  CONNECTIVITY
}