package com.damc.driver_action.ui.homeBase.summaryScreen

import android.os.Bundle
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
        val databaseClient = DatabaseClient(context)
        val appDatabase = databaseClient.getAppDatabase()
        val onDataBaseActions = appDatabase?.OnDataBaseActions()



        val userId = (requireActivity().application as AssignmentApplication).getLoginUser().userId

        lifecycleScope.launch {
            val latestTrip = withContext(Dispatchers.IO) {
                onDataBaseActions?.getLatestTrip()
            }
            val tripId = latestTrip?.id

            if (tripId != null) {
                viewModel.getTripData(tripId ,userId)
                viewModel.getTripMetricsData(tripId ,userId)
            }
        }

        val combinedData = MediatorLiveData<Pair<List<Trip>?, List<TripMetrics>?>>().apply {
            var trips: List<Trip>? = null
            var tripMetrics: List<TripMetrics>? = null

            addSource(viewModel.tripData) { newTrips ->
                trips = newTrips
                value = Pair(trips, tripMetrics)
            }

            addSource(viewModel.tripMetricsData) { newTripMetrics ->
                tripMetrics = newTripMetrics
                value = Pair(trips, tripMetrics)
            }
        }

        combinedData.observe(this, Observer { (trips, tripMetrics) ->
            if (trips != null && tripMetrics != null) {
                viewModel.adapter = SummaryAdapter(tripMetrics, trips)
                binding.rvSummary.adapter = viewModel.adapter
            }
        })

        viewModel.getActionData(userId)

        viewModel.actionData.observe(this, Observer {
            viewModel.adapter = SummaryAdapter(viewModel.actionData.value!!)
            binding.rvSummary.adapter = viewModel.adapter
        })
    }

}