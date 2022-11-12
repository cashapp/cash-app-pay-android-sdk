package com.squareup.cash.paykit

import android.os.Handler
import android.os.Looper
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData

class CashPayKit(private val clientId: String) {

  private var callbackListener: CashPayKitListener? = null

  var mainHandler: Handler = Handler(Looper.getMainLooper())

  fun createCustomerRequest(brandId: String) {
    Thread {
      val customerData = NetworkManager.createCustomerRequest(clientId, brandId)

      // TODO For now resorting to simple callbacks and thread switching. Need to investigate pros/cons of using coroutines internally as the default.
      runOnUiThread(mainHandler) {
        callbackListener?.customerCreated(customerData.customerResponseData)
      }
    }.start()
  }

  fun registerListener(listener: CashPayKitListener) {
    callbackListener = listener
  }
}

interface CashPayKitListener {
  fun customerCreated(customerData: CreateCustomerResponseData)
}

fun runOnUiThread(mainHandler: Handler, action: Runnable) {
  mainHandler.post(action)
}