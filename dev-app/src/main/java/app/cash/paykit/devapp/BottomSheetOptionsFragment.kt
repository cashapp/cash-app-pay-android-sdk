package app.cash.paykit.devapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
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
    val currentInfo = StringBuilder()
      .append("Backend Environment: ${activityViewModel.currentEnvironment}")
      .append("\n")
      .append("SDK State: ${activityViewModel.payKitState.value.javaClass.simpleName}")
      .append("\n")
      .append("Request ID: ${activityViewModel.currentRequestId}")
      .toString()
    binding.currentInfo.text = currentInfo
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
