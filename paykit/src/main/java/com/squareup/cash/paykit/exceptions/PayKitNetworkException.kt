package com.squareup.cash.paykit.exceptions

open class PayKitNetworkException(val errorType: PayKitNetworkErrorType) : Exception()

enum class PayKitNetworkErrorType {
  API,
  CONNECTIVITY
}