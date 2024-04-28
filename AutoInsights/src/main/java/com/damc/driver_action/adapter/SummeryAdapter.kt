package com.damc.driver_action.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.damc.driver_action.R
import com.damc.driver_action.domain.models.ActionData

class SummeryAdapter(private var summerData: List<ActionData>) :
    RecyclerView.Adapter<SummeryAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummeryAdapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.summery_adapter, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: SummeryAdapter.ViewHolder, position: Int) {
        holder.tvDate.text = summerData[position].date
        holder.tvHgSpeed.text = "${"%.1f".format(summerData[position].highestSpeed)} m/s"
        holder.tvHstop.text = summerData[position].hardStopCount.toString()
        holder.tvFA.text = summerData[position].fastAcceleration.toString()
    }

    override fun getItemCount(): Int {
        return summerData.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val tvHgSpeed: TextView = itemView.findViewById(R.id.tv_highest_speed)
        val tvHstop: TextView = itemView.findViewById(R.id.tv_hard_stop_count)
        val tvFA: TextView = itemView.findViewById(R.id.tv_fast_acceleration_count)
    }
}