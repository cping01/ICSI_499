package com.damc.driver_action.ui.homeBase.summaryScreen

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.damc.driver_action.R
import com.damc.driver_action.adapter.SummaryAdapter
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.databinding.FragmentSummaryBinding
import com.damc.driver_action.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SummaryFragment : BaseFragment<FragmentSummaryBinding, SummaryViewModel>() {


    override val layoutId: Int
        get() = R.layout.fragment_summary

    override val viewModel: SummaryViewModel
        get() {
            val viewModel by activityViewModel<SummaryViewModel>()
            return viewModel
        }

    companion object {
        fun newInstance() = SummaryFragment()
    }


    override fun onReady(savedInstanceState: Bundle?) {
        binding.rvSummary.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getActionData((requireActivity().application as AssignmentApplication).getLoginUser().userId)

        viewModel.actionData.observe(this, Observer {
            viewModel.adapter = SummaryAdapter(viewModel.actionData.value!!)

            binding.rvSummary.adapter = viewModel.adapter
        })
    }


}