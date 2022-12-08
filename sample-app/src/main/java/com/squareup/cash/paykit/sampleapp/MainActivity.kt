package com.squareup.cash.paykit.sampleapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.squareup.cash.paykit.CashAppPayKit
import com.squareup.cash.paykit.CashAppPayKitListener
import com.squareup.cash.paykit.PayKitState
import com.squareup.cash.paykit.PayKitState.Approved
import com.squareup.cash.paykit.PayKitState.Authorizing
import com.squareup.cash.paykit.PayKitState.CreatingCustomerRequest
import com.squareup.cash.paykit.PayKitState.Declined
import com.squareup.cash.paykit.PayKitState.NotStarted
import com.squareup.cash.paykit.PayKitState.PayKitException
import com.squareup.cash.paykit.PayKitState.PollingTransactionStatus
import com.squareup.cash.paykit.PayKitState.ReadyToAuthorize
import com.squareup.cash.paykit.PayKitState.UpdatingCustomerRequest
import com.squareup.cash.paykit.models.sdk.PayKitCurrency.USD
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTime
import com.squareup.cash.paykit.sampleapp.databinding.ActivityMainBinding

const val sandboxClientID = "CASH_CHECKOUT_SANDBOX"
const val sandboxBrandID = "BRAND_9kx6p0mkuo97jnl025q9ni94t"

const val redirectURI = "cashapppaykit://checkout"

class MainActivity : AppCompatActivity(), CashAppPayKitListener {

  private lateinit var binding: ActivityMainBinding

  private val payKitSdk = CashAppPayKit(sandboxClientID)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    registerButtons()
  }

  override fun onDestroy() {
    super.onDestroy()
    payKitSdk.unregisterFromStateUpdates()
  }

  private fun registerButtons() {
    binding.createCustomerBtn.setOnClickListener {
      payKitSdk.registerForStateUpdates(this)
      val paymentAction =
        OneTime(redirectUri = redirectURI, currency = USD, amount = 500, scopeId = sandboxBrandID)
      payKitSdk.createCustomerRequest(paymentAction)
    }

    binding.authorizeCustomerBtn.setOnClickListener {
      payKitSdk.authorizeCustomerRequest(this)
    }
  }

  /*
   * Cash App PayKit state changes.
   */

  @SuppressLint("SetTextI18n")
  override fun payKitStateDidChange(newState: PayKitState) {
    when (newState) {
      is Approved -> {
        binding.statusText.text = "APPROVED!\n\n ${newState.responseData}"
      }
      Authorizing -> {} // Ignored for now.
      CreatingCustomerRequest -> {} // Ignored for now.
      Declined -> {} // Ignored for now.
      NotStarted -> {} // Ignored for now.
      is PayKitException -> {
        Log.e("DevApp", "Got an exception from the SDK. E.: ${newState.javaClass}")
      } // Ignored for now.
      PollingTransactionStatus -> {} // Ignored for now.
      is ReadyToAuthorize -> {
        binding.statusText.text = newState.responseData.toString()
      }
      UpdatingCustomerRequest -> {} // Ignored for now.
    }
  }
}