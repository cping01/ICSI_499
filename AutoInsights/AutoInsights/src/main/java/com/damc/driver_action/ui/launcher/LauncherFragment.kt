package com.damc.driver_action.ui.launcher

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.damc.driver_action.R
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.databinding.FragmentLauncherBinding
import com.damc.driver_action.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class LauncherFragment : BaseFragment<FragmentLauncherBinding, LauncherViewModel>() {

    override val layoutId: Int = R.layout.fragment_launcher

    override val viewModel: LauncherViewModel
        get() {
            val viewModel by activityViewModel<LauncherViewModel>()
            return viewModel
        }

    override fun onReady(savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun onClickRegister(view: View) {
        viewModel.loginToRegister()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onClickLogin(view: View) {
        viewModel.validateInputs(
            binding.etUsername.text.toString(),
            binding.etPassword.text.toString(), requireContext(),
            requireActivity().application as AssignmentApplication
        )
    }


}