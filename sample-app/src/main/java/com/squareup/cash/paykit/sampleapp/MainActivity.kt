package com.squareup.cash.paykit.sampleapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.cash.paykit.CashPayKit
import com.squareup.cash.paykit.CashPayKitListener
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData
import com.squareup.cash.paykit.sampleapp.databinding.ActivityMainBinding

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

class MainActivity : AppCompatActivity(), CashPayKitListener {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.createCustomerBtn.setOnClickListener {
      val payKitSdk = CashPayKit(sandboxClientID)
      payKitSdk.registerListener(this)
      payKitSdk.createCustomerRequest(sandboxBrandID)
    }
  }

  /*
   * Cash PayKit callbacks.
   */

  override fun customerCreated(customerData: CreateCustomerResponseData) {
    binding.statusText.text = customerData.toString()
  }
}