package app.cash.paykit.core.exceptions

import app.cash.paykit.core.exceptions.PayKitNetworkErrorType.API

/**
 * This exception encapsulates all of the metadata provided by an API error.
 */
data class PayKitApiNetworkException(
  val category: String,
  val code: String,
  val detail: String?,
  val field_value: String?,
) :
  PayKitNetworkException(API)
