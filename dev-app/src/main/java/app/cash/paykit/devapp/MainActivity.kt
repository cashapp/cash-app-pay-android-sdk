package app.cash.paykit.devapp

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.cash.paykit.core.PayKitState.Approved
import app.cash.paykit.core.PayKitState.Authorizing
import app.cash.paykit.core.PayKitState.CreatingCustomerRequest
import app.cash.paykit.core.PayKitState.Declined
import app.cash.paykit.core.PayKitState.NotStarted
import app.cash.paykit.core.PayKitState.PayKitException
import app.cash.paykit.core.PayKitState.PollingTransactionStatus
import app.cash.paykit.core.PayKitState.ReadyToAuthorize
import app.cash.paykit.core.PayKitState.UpdatingCustomerRequest
import app.cash.paykit.core.models.sdk.PayKitCurrency.USD
import app.cash.paykit.core.models.sdk.PayKitPaymentAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OnFileAction
import app.cash.paykit.core.models.sdk.PayKitPaymentAction.OneTimeAction
import app.cash.paykit.devapp.databinding.ActivityMainBinding
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
      operations.createCustomerBtn.setOnClickListener {
        viewModel.createCustomerRequest(buildPaymentAction())
        hideKeyboard()
      }

      // Authorize button.
      operations.authorizeCustomerBtn.setOnClickListener {
        try {
          viewModel.authorizeCustomerRequest(this@MainActivity)
        } catch (error: Exception) {
          Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
        }
        hideKeyboard()
      }

      // Update request button.
      operations.updateCustomerBtn.setOnClickListener {
        viewModel.updateCustomerRequest(buildPaymentAction())
        hideKeyboard()
      }

      operations.resetSdkBtn.setOnClickListener {
        viewModel.resetSDK()
        hideKeyboard()
      }

      operations.retrieveExistingBtn.setOnClickListener {
        val requestId = operations.existingReqField.text.toString()
        viewModel.retrieveExistingRequest(requestId)
        hideKeyboard()
      }

      operations.resetSdkBtn.setOnClickListener {
        viewModel.resetSDK()
      }

      operations.resetSdkBtn.setOnClickListener {
        viewModel.resetSDK()
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
          val stateTextPrefix = "SDK State: "
          when (newState) {
            is Approved -> {
              binding.topAppBar.subtitle = "$stateTextPrefix Approved"
              binding.statusText.text =
                "APPROVED!\n\n ${prettyPrintDataClass(newState.responseData)}"
            }
            Authorizing -> {
              binding.topAppBar.subtitle = "$stateTextPrefix Authorizing"
            }
            CreatingCustomerRequest -> {
              binding.topAppBar.subtitle = "$stateTextPrefix CreatingCustomerRequest"
            }
            Declined -> {
              binding.topAppBar.subtitle = "$stateTextPrefix Declined"
            }
            NotStarted -> {
              binding.topAppBar.subtitle = "$stateTextPrefix NotStarted"
              binding.statusText.text = ""
            }
            is PayKitException -> {
              binding.topAppBar.subtitle = "$stateTextPrefix PayKitException (see logs)"
              binding.statusText.text = prettyPrintDataClass(newState.exception)
              Log.e(
                "DevApp",
                "Got an exception from the SDK. E.: ${newState.exception}",
              )
            } // Ignored for now.
            PollingTransactionStatus -> {
              binding.topAppBar.subtitle = "$stateTextPrefix PollingTransactionStatus"
            }
            is ReadyToAuthorize -> {
              binding.topAppBar.subtitle = "$stateTextPrefix ReadyToAuthorize"
              binding.statusText.text = prettyPrintDataClass(newState.responseData)
            }
            UpdatingCustomerRequest -> {
              binding.topAppBar.subtitle = "$stateTextPrefix UpdatingCustomerRequest"
            }
          }
        }
      }
    }
  }

  private fun hideKeyboard() {
    val imm: InputMethodManager =
      getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view: View? = currentFocus
    if (view == null) {
      view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
  }
}
