package com.damc.driver_action.adapter

import android.content.Context
import android.os.Build
import android.os.Build.VERSION
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.damc.driver_action.R
import com.damc.driver_action.app.AssignmentApplication
import com.damc.driver_action.domain.models.ActionData
import com.damc.driver_action.domain.models.Trip
import com.damc.driver_action.domain.models.TripMetrics
import com.damc.driver_action.utils.Utils.Companion.showToast
import org.eazegraph.lib.charts.PieChart
import org.eazegraph.lib.models.PieModel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryAdapter(private var summerData: List<ActionData>, private var tripMetricsData: List<TripMetrics>, private var tripData: List<Trip>) :
    RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    constructor(summerData: List<ActionData>?) : this(summerData ?: emptyList(), emptyList(), emptyList())
    constructor(tripMetricsData: List<TripMetrics>?, tripData: List<Trip>?) : this(emptyList(), tripMetricsData ?: emptyList(), tripData ?: emptyList())

    var isShowMore = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.summary_adapter, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: SummaryAdapter.ViewHolder, position: Int) {
        val tripMetrics: TripMetrics = if (position < tripMetricsData.size) tripMetricsData[position] else TripMetrics(0 ,0,0.0,0.0,0.0, 0.0, 0, 0, 0)
        holder.tvDate.text = android.icu.text.SimpleDateFormat("E MMM dd, yyyy", Locale.US)
            .format(Date())
        holder.tvHgSpeed.text = "${"%.1f".format(summerData[position].highestSpeed)} m/s"
        holder.tvHstop.text = summerData[position].hardStopCount.toString()
        holder.tvFA.text = summerData[position].fastAcceleration.toString()

        holder.llMoreData.visibility = View.GONE

        holder.btShowMore.setOnClickListener {
            if (isShowMore) {
                holder.llMoreData.visibility = View.VISIBLE
                holder.btShowMore.text = "Show Less"
            } else {
                holder.llMoreData.visibility = View.GONE
                holder.btShowMore.text = "Show More"
            }
            isShowMore = !isShowMore
        }

        // Check if there are trip data for this position
        if (position < tripData.size) {
            val trip = tripData[position]

            // Display the trip data
            holder.tvDate1.text = trip.date.toString()
            holder.tvTripId.text = "Trip ID: ${trip.id}"
        }

        // Check if there are trip metrics for this position
        if (position < tripMetricsData.size) {
            val tripMetrics = tripMetricsData[position]

            // Display the trip metrics data
            holder.tvMaxSpeed.text = "Max Speed: ${tripMetrics.maxSpeed} kph"
            holder.tvAverageSpeed.text = "Average Speed: ${tripMetrics.averageSpeed} kph"
            holder.tvTripDuration.text = "Trip Duration: ${tripMetrics.tripDuration} minutes"
            holder.tvTripDistance.text = "Trip Distance: ${tripMetrics.tripDistance} km"
            holder.tvSpeedingInstances.text = "Speeding Instances: ${tripMetrics.speedingInstances}"
            holder.tvHardAccelerationInstances.text = "Hard Acceleration Instances: ${tripMetrics.hardAccelerationInstances}"
            holder.tvHardBrakingInstances.text = "Hard Braking Instances: ${tripMetrics.hardBrakingInstances}"
        }


        var totalAction =
            tripMetrics.hardBrakingInstances + summerData[position].mediumAcceleration
        +summerData[position].goodAcceleration + tripMetrics.hardAccelerationInstances
        +summerData[position].mediumStopCount + summerData[position].goodStopCount



        holder.pieChart.addPieSlice(
            PieModel(
                "Hard Acceleration",
                tripMetrics.hardAccelerationInstances.toFloat(),
                holder.itemView.context.resources.getColor(R.color.hard_acceleration)
            )
        )

        holder.pieChart.addPieSlice(
            PieModel(
                "Medium Acceleration",
                summerData[position].mediumAcceleration.toFloat(),
                holder.itemView.context.resources.getColor(R.color.medium_acceleration)
            )
        )

        holder.pieChart.addPieSlice(
            PieModel(
                "Good Acceleration",
                summerData[position].goodAcceleration.toFloat(),
                holder.itemView.context.resources.getColor(R.color.good_acceleration)
            )
        )

        holder.pieChart.addPieSlice(
            PieModel(
                "Hard Stop",
                tripMetrics.hardBrakingInstances.toFloat(),
                holder.itemView.context.resources.getColor(R.color.hard_stop)
            )
        )

        holder.pieChart.addPieSlice(
            PieModel(
                "Medium Stop",
                summerData[position].goodStopCount.toFloat(),
                holder.itemView.context.resources.getColor(R.color.medium_stop)
            )
        )

        holder.pieChart.addPieSlice(
            PieModel(
                "Good Stop",
                summerData[position].goodStopCount.toFloat(),
                holder.itemView.context.resources.getColor(R.color.good_stop)
            )
        )
        holder.pieChart.startAnimation()


        holder.btSaveData.setOnClickListener {
            val dataToSave =
                "----------------------START------------------------------\n " +
                        "Username - ${(holder.itemView.context.applicationContext as AssignmentApplication).getLoginUser().username}\n" +
                        "Date - ${holder.tvDate1.text}\n" +
                        "Trip Duration: ${tripMetrics.tripDuration} minutes\n" +
                        "Trip Distance: ${tripMetrics.tripDistance}\nkm"
                        "Total Driver Action count - ${totalAction}\n" +
                        "Highest Speed -  ${tripMetrics.maxSpeed}\n" +
                                "Speeding Instances: ${tripMetrics.speedingInstances}\n" +
                                "Hard Stop Instances: ${tripMetrics.hardBrakingInstances}\n" +
                        "Medium Stop Count - ${summerData[position].mediumStopCount}\n" +
                        "Good Stop Count - ${summerData[position].goodStopCount}\n" +
                                "Hard Acceleration Instances: ${tripMetrics.hardAccelerationInstances}\n" +
                        "Medium Acceleration Count - ${summerData[position].mediumAcceleration}\n" +
                        "Good Acceleration Count - ${summerData[position].goodStopCount}\n" +
                        "-----------------------END------------------"

            saveTextFile(dataToSave, holder.itemView.context)
        }
    }

    override fun getItemCount(): Int {
        return summerData.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Add TextViews for the trip metrics
        //new data
        val tvDate1: TextView = itemView.findViewById(R.id.tv_date1)
        val tvTripId: TextView = itemView.findViewById(R.id.tv_trip_id)
        val tvMaxSpeed: TextView = itemView.findViewById(R.id.tv_max_speed)
        val tvAverageSpeed: TextView = itemView.findViewById(R.id.tv_average_speed)
        val tvTripDuration: TextView = itemView.findViewById(R.id.tv_trip_duration)
        val tvTripDistance: TextView = itemView.findViewById(R.id.tv_trip_distance)
        val tvSpeedingInstances: TextView = itemView.findViewById(R.id.tv_speeding_instances)
        val tvHardAccelerationInstances: TextView = itemView.findViewById(R.id.tv_hard_acceleration_instances)
        val tvHardBrakingInstances: TextView = itemView.findViewById(R.id.tv_hard_braking_instances)
        //old
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvHgSpeed: TextView = itemView.findViewById(R.id.tv_highest_speed)
        val tvHstop: TextView = itemView.findViewById(R.id.tv_hard_stop_count)
        val tvFA: TextView = itemView.findViewById(R.id.tv_fast_acceleration_count)
        val pieChart: PieChart = itemView.findViewById(R.id.piechart)
        val btShowMore: TextView = itemView.findViewById(R.id.bt_show_more)
        val llMoreData: LinearLayout = itemView.findViewById(R.id.ll_more_data)
        val btSaveData: TextView = itemView.findViewById(R.id.bt_save_data)
    }

    fun saveTextFile(text: String?, context: Context) {
        var creFile = false
        val file: File
        file = if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/AutoInsights"
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/AutoInsights")
        }
        if (!file.exists()) {
            try {
                creFile = file.mkdir()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            creFile = true
        }

        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val currentDate = dateFormat.format(Calendar.getInstance().time)

        if (creFile) {
            val fileNew: File?
            if (VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fileNew =
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            .toString() + "/AutoInsights/" + currentDate + ".txt"
                    )
            } else {
                fileNew = File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/AutoInsights/" + currentDate + ".txt"
                )
            }

            if (!fileNew.exists()) {
                try {
                    file.createNewFile()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                val buf = BufferedWriter(FileWriter(fileNew, true))
                buf.append(text)
                buf.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            showToast("File Saved Successfully", context)
        }


    }
}