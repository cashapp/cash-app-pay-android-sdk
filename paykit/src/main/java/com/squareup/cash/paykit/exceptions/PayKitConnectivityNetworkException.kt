package com.squareup.cash.paykit.exceptions

import com.squareup.cash.paykit.exceptions.PayKitNetworkErrorType.CONNECTIVITY

/**
 * This exception represents Network Not Available.
 */
class PayKitConnectivityNetworkException :
  PayKitNetworkException(CONNECTIVITY)