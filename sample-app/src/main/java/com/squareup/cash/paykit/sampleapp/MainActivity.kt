package com.squareup.cash.paykit.sampleapp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.cash.paykit.CashPayKit
import com.squareup.cash.paykit.CashPayKitListener
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

class MainActivity : AppCompatActivity(), CashPayKitListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    findViewById<Button>(R.id.button).setOnClickListener {
      val payKitSdk = CashPayKit(sandboxClientID)
      payKitSdk.registerListener(this)
      payKitSdk.createCustomerRequest(sandboxBrandID)
    }
  }

  /*
   * Cash PayKit callbacks.
   */

  override fun customerCreated(customerData: CreateCustomerResponseData) {
    findViewById<TextView>(R.id.statusText).text = customerData.toString()
  }
}