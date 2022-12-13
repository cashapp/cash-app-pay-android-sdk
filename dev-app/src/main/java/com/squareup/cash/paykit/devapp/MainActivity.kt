package com.squareup.cash.paykit.devapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
import com.squareup.cash.paykit.devapp.databinding.ActivityMainBinding
import com.squareup.cash.paykit.models.sdk.PayKitCurrency.USD
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OnFileAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTimeAction

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
    binding.apply {
      createCustomerBtn.setOnClickListener {
        payKitSdk.registerForStateUpdates(this@MainActivity)

        val paymentAction = if (toggleButton.checkedButtonId == R.id.oneTimeButton) {
          OneTimeAction(
            redirectUri = redirectURI,
            currency = USD,
            amount = amountField.text.toString().toIntOrNull(),
            scopeId = sandboxBrandID
          )
        } else {
          OnFileAction(
            redirectUri = redirectURI,
            scopeId = sandboxBrandID,
            accountReferenceId = referenceField.text.toString()
          )
        }

        payKitSdk.createCustomerRequest(paymentAction)
      }

      authorizeCustomerBtn.setOnClickListener {
        payKitSdk.authorizeCustomerRequest(this@MainActivity)
      }

      // Toggle Buttons.
      oneTimeButton.setOnClickListener {
        amountContainer.isVisible = true
        referenceContainer.isVisible = false
      }
      onFileButton.setOnClickListener {
        amountContainer.isVisible = false
        referenceContainer.isVisible = true
      }
    }
  }

  /*
   * Cash App PayKit state changes.
   */

  @SuppressLint("SetTextI18n")
  override fun payKitStateDidChange(newState: PayKitState) {
    when (newState) {
      is Approved -> {
        binding.statusText.text = "APPROVED!\n\n ${prettyPrintDataClass(newState.responseData)}"
      }
      Authorizing -> {} // Ignored for now.
      CreatingCustomerRequest -> {} // Ignored for now.
      Declined -> {} // Ignored for now.
      NotStarted -> {} // Ignored for now.
      is PayKitException -> {
        binding.statusText.text = prettyPrintDataClass(newState.exception)
        Log.e(
          "DevApp",
          "Got an exception from the SDK. E.: ${newState.exception}"
        )
      } // Ignored for now.
      PollingTransactionStatus -> {} // Ignored for now.
      is ReadyToAuthorize -> {
        binding.statusText.text = prettyPrintDataClass(newState.responseData)
      }
      UpdatingCustomerRequest -> {} // Ignored for now.
    }
  }
}
