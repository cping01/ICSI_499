package com.damc.driver_action.ui.register

import android.os.Bundle
import android.view.View
import com.damc.driver_action.R
import com.damc.driver_action.databinding.FragmentRegisterBinding
import com.damc.driver_action.ui.BaseFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class RegisterFragment : BaseFragment<FragmentRegisterBinding, RegisterViewModel>() {

    override val layoutId: Int = R.layout.fragment_register
    override val viewModel: RegisterViewModel
        get() {
            val viewModel by activityViewModel<RegisterViewModel>()
            return viewModel
        }

    override fun onReady(savedInstanceState: Bundle?) {
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    fun onClickLogin(view: View) {
        viewModel.registerToLogin()
    }

    fun onClickRegister(view: View) {
        GlobalScope.launch {
            if (viewModel.validateFields(
                    binding.etUsername.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etPasswordConfirm.text.toString(), requireContext()
                )
            ) {

            }
        }
    }


}