package com.damc.driver_action.ui.homeBase

import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.damc.driver_action.R
import com.damc.driver_action.databinding.FragmentHomeBaseBinding
import com.damc.driver_action.ui.BaseFragment
import com.damc.driver_action.ui.homeBase.home.HomeScreen
import com.damc.driver_action.ui.homeBase.settings.SettingsFragment
import com.damc.driver_action.ui.homeBase.summaryScreen.SummaryFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class HomeBaseFragment : BaseFragment<FragmentHomeBaseBinding, HomeBaseViewModel>(),
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        fun newInstance() = HomeBaseFragment()
    }

    override val layoutId: Int
        get() = R.layout.fragment_home_base

    override val viewModel: HomeBaseViewModel
        get() {
            val viewModel by activityViewModel<HomeBaseViewModel>()
            return viewModel
        }

    override fun onReady(savedInstanceState: Bundle?) {
        binding.bottomNavigationView.setOnItemSelectedListener(this)
        binding.bottomNavigationView.selectedItemId = R.id.it_home
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.it_home -> {
                setCurrentFragment(HomeScreen.newInstance())
                return true
            }

            R.id.it_summery -> {
                setCurrentFragment(SummaryFragment.newInstance())
                return true
            }

            R.id.it_settings -> {
                setCurrentFragment(SettingsFragment.newInstance())
                return true
            }

            R.id.it_logout -> {
                viewModel.logOut()
            }
        }
        return true
    }

    private fun setCurrentFragment(fragment: Fragment) {
        requireActivity().getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fl_fragment, fragment)
            .commit()
    }


}