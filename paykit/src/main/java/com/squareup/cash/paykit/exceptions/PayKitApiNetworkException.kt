package com.squareup.cash.paykit.exceptions

import com.squareup.cash.paykit.exceptions.PayKitNetworkErrorType.API

data class PayKitApiNetworkException(
  val category: String,
  val code: String,
  val detail: String?,
  val field: String?
) :
  PayKitNetworkException(API)