package com.example.prepx.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.prepx.databinding.DialogAppGuideBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Interactive App Guide & Onboarding Bottom Sheet Dialog.
 */
class AppGuideBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAppGuideBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGotIt.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AppGuideBottomSheetDialog"

        fun newInstance(): AppGuideBottomSheetDialog {
            return AppGuideBottomSheetDialog()
        }
    }
}
