package com.example.hydro30

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DayAdapter(private val days: List<String>) :
    RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private val today =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

    private val DAILY_GOAL = 2500 // ml

    class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: FrameLayout = view.findViewById(R.id.dayContainer)
        val waterFill: View = view.findViewById(R.id.waterFill)
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val imgDone: ImageView = view.findViewById(R.id.imgDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]
        val context = holder.container.context

        holder.tvDay.text = "Day ${position + 1}"
        holder.tvDate.text = date

        val isToday = date == today

        // 🔄 Always update visuals
        updateWaterFill(holder, date)
        updateDoneIcon(holder, date)

        // Background color
        holder.container.setBackgroundColor(
            if (isToday) Color.parseColor("#E8F5E9")
            else Color.parseColor("#E3F2FD")
        )

        // 👉 Click → open details page
        holder.container.setOnClickListener {
            context.startActivity(
                Intent(context, TodayWaterActivity::class.java)
                    .putExtra("DATE", date)
            )
        }
        // ✋ Long press on TODAY block → add water
        // ✋ Long press ONLY on today block → open add dialog
        // ✋ Long press ONLY on today block → add water dialog (NO navigation)
        if (isToday) {
            holder.container.setOnLongClickListener {
                if (context is ChallengeActivity) {
                    context.showAddWaterDialog()
                }
                true
            }
        } else {
            holder.container.setOnLongClickListener(null)
        }



    }

    override fun getItemCount(): Int = days.size

    // 🔑 Convert date → preference key
    private fun prefKeyFromDate(date: String): String {
        return date.replace("/", "_") // dd_MM_yyyy
    }

    // 💧 Update water fill for ANY date
    private fun updateWaterFill(holder: DayViewHolder, date: String) {
        val prefs =
            holder.container.context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)

        val key = prefKeyFromDate(date)
        val waterMl = prefs.getInt(key, 0).coerceAtMost(DAILY_GOAL)
        val percent = waterMl.toFloat() / DAILY_GOAL

        holder.container.post {
            val fullHeight = holder.container.height
            val fillHeight = (fullHeight * percent).toInt()

            val params = holder.waterFill.layoutParams
            params.height = fillHeight
            holder.waterFill.layoutParams = params
        }
    }

    // ✅ Show checkmark if that day reached goal
    private fun updateDoneIcon(holder: DayViewHolder, date: String) {
        val prefs =
            holder.container.context.getSharedPreferences("water_prefs", Context.MODE_PRIVATE)

        val key = prefKeyFromDate(date)
        val waterMl = prefs.getInt(key, 0)

        holder.imgDone.visibility =
            if (waterMl >= DAILY_GOAL) View.VISIBLE else View.GONE
    }
}
