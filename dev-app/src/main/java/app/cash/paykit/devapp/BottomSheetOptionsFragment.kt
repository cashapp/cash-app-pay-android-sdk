package app.cash.paykit.devapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import app.cash.paykit.core.R
import app.cash.paykit.devapp.SDKEnvironments.SANDBOX
import app.cash.paykit.devapp.SDKEnvironments.STAGING
import app.cash.paykit.devapp.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetOptionsFragment : BottomSheetDialogFragment() {

  private var _binding: FragmentBottomSheetBinding? = null

  private val activityViewModel: MainActivityViewModel by activityViewModels()

  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    _binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    updateCurrentInfo()

    // Environment Toggle Buttons.
    binding.sandboxButton.setOnClickListener {
      activityViewModel.currentEnvironment = SANDBOX
      activityViewModel.resetSDK()
      updateCurrentInfo()
    }
    binding.stagingButton.setOnClickListener {
      activityViewModel.currentEnvironment = STAGING
      activityViewModel.resetSDK()
      updateCurrentInfo()
    }

    binding.payKitButtonLight.setOnClickListener {
      Toast.makeText(requireContext(), "Light Button pressed", Toast.LENGTH_SHORT).show()
    }

    binding.payKitButton2.setOnClickListener {
      Toast.makeText(requireContext(), "Dark Button pressed", Toast.LENGTH_SHORT).show()
    }

    binding.payKitButtonLightDisabled.isEnabled = false
    binding.payKitButtonDarkDisabled.isEnabled = false
  }

  private fun updateCurrentInfo() {
    val currentInfo = StringBuilder()
      .append("Backend Environment: ${activityViewModel.currentEnvironment}")
      .append("\n")
      .append("SDK State: ${activityViewModel.payKitState.value.javaClass.simpleName}")
      .append("\n")
      .append("Client ID: ${activityViewModel.clientId}")
      .append("\n")
      .append("Brand ID: ${activityViewModel.brandId}")
      .append("\n")
      .append("Request ID: ${activityViewModel.currentRequestId}")
      .append("\n")
      .append("SDK Version: ${getString(R.string.cashpaykit_version)}")
      .toString()
    binding.currentInfo.text = currentInfo
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}