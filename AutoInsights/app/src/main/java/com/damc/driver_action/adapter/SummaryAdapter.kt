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

class SummaryAdapter(private var summerData: List<ActionData>) :
    RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

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
            val dataToSave =
                "----------------------START------------------------------\n " +
                        "Username - ${(holder.itemView.context.applicationContext as AssignmentApplication).getLoginUser().username}\n" +
                        "Total Driver Action count - ${totalAction}\n" +
                        "Highest Speed -  ${summerData[position].highestSpeed}\n" +
                        "Hard Stop Count - ${summerData[position].hardStopCount}\n" +
                        "Medium Stop Count - ${summerData[position].mediumStopCount}\n" +
                        "Good Stop Count - ${summerData[position].goodStopCount}\n" +
                        "Hard Acceleration Count - ${summerData[position].fastAcceleration}\n" +
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