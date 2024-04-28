package com.damc.driver_action.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation

abstract class BaseFragment<BINDING : ViewDataBinding, VM : BaseViewModel>() : Fragment() {
    @get:LayoutRes
    protected abstract val layoutId: Int
    protected abstract val viewModel: VM
    protected lateinit var binding: BINDING

    var navController: NavController? = null

    protected val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Do nothing to prevent back navigation
        }
    }

    protected abstract fun onReady(savedInstanceState: Bundle?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            layoutInflater, layoutId, container, false
        )

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            setVariable(BR.viewmodel, viewModel)
            setVariable(BR.fragment, this@BaseFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        observeNavigation()
        onReady(savedInstanceState)
    }

    private fun observeNavigation() {
        viewModel.navigation.observeNonNull(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { navigationCommand ->
                handleNavigation(navigationCommand)
            }
        }
    }

    private fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (t: T) -> Unit) {
        this.observe(
            owner
        ) {
            it?.let(observer)
        }
    }


    private fun handleNavigation(navCommand: NavigationCommand) {
        when (navCommand) {
            is NavigationCommand.ToDirection -> navController?.navigate(navCommand.directions)
            is NavigationCommand.Back -> navController?.navigateUp()
        }
    }
}