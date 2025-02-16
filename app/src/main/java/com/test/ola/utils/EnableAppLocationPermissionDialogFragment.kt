package com.test.ola.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.test.ola.databinding.FragmentEnableAppLocationPermissionDialogBinding

class EnableAppLocationPermissionDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentEnableAppLocationPermissionDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEnableAppLocationPermissionDialogBinding.inflate(layoutInflater, container, false)

        binding.goToSetting.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
            dismiss()
        }

        return binding.root
    }

}
