package app.cash.paykit.core.exceptions

import app.cash.paykit.core.exceptions.PayKitNetworkErrorType.CONNECTIVITY

/**
 * This exception represents Network connectivity issues, such as network timeout errors.
 */
data class PayKitConnectivityNetworkException(val e: Exception) :
  PayKitNetworkException(CONNECTIVITY)
