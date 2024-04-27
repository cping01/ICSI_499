package com.damc.driver_action.ui.homeBase.settings

import android.os.Bundle
import com.damc.driver_action.R
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.databinding.FragmentSettingsBinding
import com.damc.driver_action.ui.BaseFragment
import com.damc.driver_action.utils.Utils
import com.damc.driver_action.utils.Utils.Companion.showToast
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_settings

    override val viewModel: SettingsViewModel
        get() {
            val viewModel by activityViewModel<SettingsViewModel>()
            return viewModel
        }

    override fun onReady(savedInstanceState: Bundle?) {

        viewModel.users = (requireActivity().application as AssignmentApplication).getLoginUser()

        binding.cbBiometrics.isChecked = viewModel.users.biometricsEnabled

        binding.cbDriverTracking.isChecked = !viewModel.users.autoTrackingEnabled

        binding.cbBiometrics.setOnCheckedChangeListener { compoundButton, b ->
            if (Utils.isBiometricReady(requireContext())) {
                viewModel.users.biometricsEnabled = b
                (requireActivity().application as AssignmentApplication).setLoginUser(viewModel.users)
                viewModel.upDateUser(viewModel.users)
                viewModel.preferenceRepository.saveBiometricEnabled(true)
            } else {
                showToast("Device does not support biometrics", requireContext())
                binding.cbBiometrics.isChecked = false
            }
        }

        binding.cbDriverTracking.setOnCheckedChangeListener{
            comb, b ->
            viewModel.users.autoTrackingEnabled = !b
            (requireActivity().application as AssignmentApplication).setLoginUser(viewModel.users)
            viewModel.upDateUser(viewModel.users)
        }
    }


}