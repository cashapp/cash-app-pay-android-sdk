package com.squareup.cash.paykit.exceptions

import com.squareup.cash.paykit.exceptions.PayKitNetworkErrorType.CONNECTIVITY

/**
 * This exception represents Network connectivity issues, such as network timeout errors.
 */
data class PayKitConnectivityNetworkException(val e: Exception) :
  PayKitNetworkException(CONNECTIVITY)
