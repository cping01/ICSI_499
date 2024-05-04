package com.damc.driver_action.adapter

import android.content.Context
import android.content.Intent
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
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SummaryAdapter(private var summerData: List<ActionData>, private var tripData: List<Trip>, private var tripMetricsData: List<TripMetrics>) :
    RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    // Declare the variables
    private var actionData: List<ActionData> = listOf()
    private var trips: List<Trip> = listOf()
    private var tripMetrics: List<TripMetrics> = listOf()
    private var dataToSave: String = ""
    var isShowMore = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.summary_adapter, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: SummaryAdapter.ViewHolder, position: Int) {
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


        var totalAction =
            summerData[position].hardStopCount + summerData[position].mediumAcceleration
        +summerData[position].goodAcceleration + summerData[position].hardStopCount
        +summerData[position].mediumStopCount + summerData[position].goodStopCount


        holder.pieChart.addPieSlice(
            PieModel(
                "Hard Acceleration",
                summerData[position].fastAcceleration.toFloat(),
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
                summerData[position].hardStopCount.toFloat(),
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
              dataToSave =
                        "--------------------START--------------------\n " +
                        "Username : ${(holder.itemView.context.applicationContext as AssignmentApplication).getLoginUser().username}\n" +
                        "Date : ${holder.tvDate.text}\n" +
                        "Total Driver Action Count : ${totalAction}\n" +
                        "Trip Duration : ${tripMetricsData[position].tripDuration} minutes" +
                        "Trip Distance : ${tripMetricsData[position].tripDistance} miles" +
                        "Highest Speed :  ${summerData[position].highestSpeed} kph\n" +
                        "Average Speed : ${tripMetricsData[position].averageSpeed} mph\n" +
                        "Speeding Count : ${tripMetricsData[position].speedingInstances}" +
                        "Hard Stop Count : ${summerData[position].hardStopCount}\n" +
                        "Medium Stop Count : ${summerData[position].mediumStopCount}\n" +
                        "Good Stop Count : ${summerData[position].goodStopCount}\n" +
                        "Hard Acceleration Count : ${summerData[position].fastAcceleration}\n" +
                        "Medium Acceleration Count : ${summerData[position].mediumAcceleration}\n" +
                        "Good Acceleration Count : ${summerData[position].goodStopCount}\n" +
                        "---------------------END---------------------"

            saveTextFile(dataToSave, holder.itemView.context)
        }
    }

    fun updateData(actionData: List<ActionData>, trips: List<Trip>, tripMetrics: List<TripMetrics>) {
        // Update the data and notify the adapter
        this.actionData = actionData
        this.trips = trips
        this.tripMetrics = tripMetrics
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return summerData.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvHgSpeed: TextView = itemView.findViewById(R.id.tv_highest_speed)
        val tvHstop: TextView = itemView.findViewById(R.id.tv_hard_stop_count)
        val tvFA: TextView = itemView.findViewById(R.id.tv_fast_acceleration_count)
        val pieChart: PieChart = itemView.findViewById(R.id.piechart)
        val btShowMore: TextView = itemView.findViewById(R.id.bt_show_more)
        val llMoreData: LinearLayout = itemView.findViewById(R.id.ll_more_data)
        val btSaveData: TextView = itemView.findViewById(R.id.bt_save_data)

        val btnSendEmail: TextView = itemView.findViewById(R.id.bt_send_email)
        init {
            btnSendEmail.setOnClickListener { view ->
                sendEmailAction(view.context)
            }
        }
    }

    private fun sendEmailAction(context: Context) {
        // Read the content from the saved .txt file
        val savedText = readSavedFileContent(context)

        // Create an intent for sending email
        val emailIntent = Intent(Intent.ACTION_SEND)
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "AutoInsights Data $currentDate")
        emailIntent.putExtra(Intent.EXTRA_TEXT, dataToSave)

        // Start the activity for sending email
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    private fun readSavedFileContent(context: Context): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm")
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        // Read the content from the saved .txt file
        val file = File(context.getExternalFilesDir(null), "AutoInsights/AutoInsights Data $currentDate.txt")
        val content = StringBuilder()

        try {
            val reader = BufferedReader(FileReader(file))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                content.append(line).append("\n")
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return content.toString()
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
                            .toString() + "/AutoInsights/" +"AutoInsights Data $currentDate" + ".txt"
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