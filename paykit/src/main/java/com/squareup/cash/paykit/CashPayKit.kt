package com.squareup.cash.paykit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData

class CashPayKit(private val clientId: String) {

  private var callbackListener: CashPayKitListener? = null
  private var customerResponseData: CreateCustomerResponseData? = null

  var mainHandler: Handler = Handler(Looper.getMainLooper())

  /**
   * @param brandId The Brand Identifier that was provided to you by the Cash Pay console.
   * @param referenceURI: The URI to deep link back into your application once the transaction is approved.
   */
  fun createCustomerRequest(brandId: String, redirectUri: String) {
    // TODO: print error if there is no listener registered.
    Thread {
      val customerData = NetworkManager.createCustomerRequest(clientId, brandId, redirectUri)

      // TODO For now resorting to simple callbacks and thread switching. Need to investigate pros/cons of using coroutines internally as the default.
      runOnUiThread(mainHandler) {
        customerResponseData = customerData.customerResponseData
        callbackListener?.customerCreated(customerData.customerResponseData)
      }
    }.start()
  }

  fun authorizeCustomer(context: Context) {
    val customerData = customerResponseData
      ?: throw NullPointerException("Can't call authorize user before calling `createCustomerRequest`. Alternatively provide your own customerData")

    authorizeCustomer(context, customerData)
  }

  fun authorizeCustomer(context: Context, customerData: CreateCustomerResponseData) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(customerData.authFlowTriggers.mobileUrl)
    context.startActivity(intent)
  }

  /**
   *  Register listener to receive PayKit callbacks.
   */
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