package com.damc.driver_action.ui.launcher

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import com.damc.driver_action.R
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.databinding.FragmentLauncherBinding
import com.damc.driver_action.ui.BaseFragment
import com.damc.driver_action.utils.BiometricAuthListener
import com.damc.driver_action.utils.BiometricUtils
import com.damc.driver_action.utils.Utils
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LauncherFragment : BaseFragment<FragmentLauncherBinding, LauncherViewModel>(),
    BiometricAuthListener {

    override val layoutId: Int = R.layout.fragment_launcher

    override val viewModel: LauncherViewModel
        get() {
            val viewModel by activityViewModel<LauncherViewModel>()
            return viewModel
        }

    override fun onReady(savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setUsername()

        setBiometricsView(binding.etUsername.text.toString())

        usernameChangedListener()

    }

    fun onClickBiometrics(view: View) {
        if (Utils.isBiometricReady(requireContext())) {
            BiometricUtils.showBiometricPrompt(
                activity = requireActivity(),
                listener = this,
                cryptoObject = null,
            )
        }
    }

    fun usernameChangedListener() {
        binding.etUsername.addTextChangedListener { char ->
            if (viewModel.preferenceRepository.getBiometricEnabled()) {
                setBiometricsView(char.toString())
            }
        }
    }

    fun setBiometricsView(text: String) {
        if (viewModel.preferenceRepository.getBiometricEnabled() && text == viewModel.preferenceRepository.getUsername()) {
            binding.etPassword.visibility = View.VISIBLE
            binding.imBiometrics.visibility = View.VISIBLE
        } else {
            binding.etPassword.visibility = View.VISIBLE
            binding.imBiometrics.visibility = View.GONE
        }
    }

    fun onClickRegister(view: View) {
        viewModel.loginToRegister()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickLogin(view: View) {
        if (viewModel.validateInputs(
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString(),
                requireContext(),
                false,
                requireActivity().application as AssignmentApplication
            )
        ) {
            binding.etPassword.setText("")
        }
    }

    fun setUsername() {
        val username = viewModel.preferenceRepository.getUsername()
        if (username.isNotEmpty()) {
            binding.etUsername.setText(username)
        }
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        Utils.showToast(errMsg, requireContext())
    }

    override fun onBiometricAuthenticateSuccess(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
        if (viewModel.validateInputs(
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString(),
                requireContext(),
                true,
                requireActivity().application as AssignmentApplication
            )
        ) {
            binding.etPassword.setText("")
        }
    }


}