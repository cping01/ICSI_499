package com.damc.driver_action.ui.homeBase.summaryScreen

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.damc.driver_action.R
import com.damc.driver_action.adapter.SummaryAdapter
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.data.local.room.DatabaseClient
import com.damc.driver_action.databinding.FragmentSummaryBinding
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.ui.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        viewModel.adapter = SummaryAdapter(emptyList(), emptyList(), emptyList())
        binding.rvSummary.adapter = viewModel.adapter
        val databaseClient = DatabaseClient(context)
        val appDatabase = databaseClient.getAppDatabase()
        val onDataBaseActions = appDatabase?.OnDataBaseActions()

        val userId = (requireActivity().application as AssignmentApplication).getLoginUser().userId

        lifecycleScope.launch {
            val latestTrip = withContext(Dispatchers.IO) {
                onDataBaseActions?.getLatestTrip(userId)
            }
            val tripId = latestTrip?.id
            Log.d("SummaryFragment", "tripId: $tripId")

            if (tripId != null) {
                viewModel.getActionData(userId)
                viewModel.getTripData(tripId, userId)
                viewModel.getTripMetricsData(tripId, userId)

                viewModel.actionData.observe(viewLifecycleOwner) { actionData ->
                    Log.d("SummaryFragment", "actionData: $actionData")
                    viewModel.tripData.observe(viewLifecycleOwner) { trips ->
                        Log.d("SummaryFragment", "trips: $trips")
                        viewModel.tripMetricsData.observe(viewLifecycleOwner) { tripMetrics ->
                            Log.d("SummaryFragment", "tripMetrics: $tripMetrics")
                            // Update the data in the adapter
                            viewModel.adapter.updateData(
                                actionData ?: emptyList(),
                                trips ?: emptyList(),
                                tripMetrics ?: emptyList()
                            )
                            Log.d("SummaryFragment", "SummaryAdapter is set to the RecyclerView")
                        }
                    }
                }
            }
        }

        Log.d("SummaryFragment", "getActionData is called")
    }
}