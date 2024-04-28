package com.damc.driver_action.ui.summerScreen

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.damc.driver_action.R
import com.damc.driver_action.adapter.SummeryAdapter
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.databinding.FragmentSummeryBinding
import com.damc.driver_action.ui.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SummeryFragment : BaseFragment<FragmentSummeryBinding, SummeryViewModel>() {


    override val layoutId: Int
        get() = R.layout.fragment_summery

    override val viewModel: SummeryViewModel
        get() {
            val viewModel by activityViewModel<SummeryViewModel>()
            return viewModel
        }


    override fun onReady(savedInstanceState: Bundle?) {
        binding.rvSummery.layoutManager = LinearLayoutManager(requireContext())

        viewModel.getActionData((requireActivity().application as AssignmentApplication).getLoginUser().userId)

        viewModel.actionData.observe(this, Observer {
            viewModel.adapter = SummeryAdapter(viewModel.actionData.value!!)

            binding.rvSummery.adapter = viewModel.adapter
        })
    }


}