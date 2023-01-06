package com.squareup.cash.paykit.devapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OnFileAction
import com.squareup.cash.paykit.models.sdk.PayKitPaymentAction.OneTimeAction
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private val viewModel: MainActivityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    registerButtons()
    handlePayKitStateChanges()
  }

  private fun registerButtons() {
    binding.apply {
      // Create Customer button.
      createCustomerBtn.setOnClickListener {
        viewModel.createCustomerRequest(buildPaymentAction())
      }

      // Authorize button.
      authorizeCustomerBtn.setOnClickListener {
        try {
          viewModel.authorizeCustomerRequest(this@MainActivity)
        } catch (error: Exception) {
          Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
        }
      }

      // Update request button.
      updateCustomerBtn.setOnClickListener {
        viewModel.updateCustomerRequest(buildPaymentAction())
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

  /**
   * Produce a [PayKitPaymentAction] payload based on the current form parameters.
   */
  private fun buildPaymentAction(): PayKitPaymentAction {
    val amount = binding.amountField.text.toString().toIntOrNull()
    val currency = if (amount == null) null else USD
    return if (binding.toggleButton.checkedButtonId == R.id.oneTimeButton) {
      OneTimeAction(
        redirectUri = redirectURI,
        currency = currency,
        amount = amount,
        scopeId = sandboxBrandID,
      )
    } else {
      OnFileAction(
        redirectUri = redirectURI,
        scopeId = sandboxBrandID,
        accountReferenceId = binding.referenceField.text.toString(),
      )
    }
  }

  /*
   * Cash App PayKit state changes.
   */

  @SuppressLint("SetTextI18n")
  private fun handlePayKitStateChanges() {
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.payKitState.collect { newState ->
          when (newState) {
            is Approved -> {
              binding.statusText.text =
                "APPROVED!\n\n ${prettyPrintDataClass(newState.responseData)}"
            }
            Authorizing -> {} // Ignored for now.
            CreatingCustomerRequest -> {} // Ignored for now.
            Declined -> {} // Ignored for now.
            NotStarted -> {} // Ignored for now.
            is PayKitException -> {
              binding.statusText.text = prettyPrintDataClass(newState.exception)
              Log.e(
                "DevApp",
                "Got an exception from the SDK. E.: ${newState.exception}",
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
    }
  }
}
