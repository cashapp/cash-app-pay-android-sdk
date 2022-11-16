package com.squareup.cash.paykit.sampleapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.cash.paykit.CashPayKit
import com.squareup.cash.paykit.CashPayKitListener
import com.squareup.cash.paykit.models.response.CreateCustomerResponseData
import com.squareup.cash.paykit.sampleapp.databinding.ActivityMainBinding

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

const val redirectURI = "cashpaykit://checkout"

class MainActivity : AppCompatActivity(), CashPayKitListener {

  private lateinit var binding: ActivityMainBinding

  private val payKitSdk = CashPayKit(lifecycle, sandboxClientID)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    registerButtons()
  }

  private fun registerButtons() {
    binding.createCustomerBtn.setOnClickListener {
      payKitSdk.registerListener(this)
      payKitSdk.createCustomerRequest(sandboxBrandID, redirectURI)
    }

    binding.authorizeCustomerBtn.setOnClickListener {
      payKitSdk.authorizeCustomer(this)
    }
  }

  /*
   * Cash PayKit callbacks.
   */

  override fun customerCreated(customerData: CreateCustomerResponseData) {
    binding.statusText.text = customerData.toString()
  }

  @SuppressLint("SetTextI18n")
  override fun transactionFinished(wasSuccessful: Boolean) {
    binding.statusText.text = "APPROVED!\n\n ${payKitSdk.customerResponseData?.toString()}"
  }
}