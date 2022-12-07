package com.squareup.cash.paykit.exceptions

import com.squareup.cash.paykit.exceptions.PayKitNetworkErrorType.CONNECTIVITY

class PayKitConnectivityNetworkException :
  PayKitNetworkException(CONNECTIVITY)