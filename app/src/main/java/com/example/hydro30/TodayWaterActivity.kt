package com.example.hydro30

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TodayWaterActivity : AppCompatActivity() {

    private val DAILY_GOAL = 2500

    private lateinit var tvWater: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var btnRemove: Button
    private lateinit var actionButtons: View

    private lateinit var prefsKey: String
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_today_water)

        tvWater = findViewById(R.id.tvWater)
        recycler = findViewById(R.id.recyclerLogs)
        btnAdd = findViewById(R.id.btnAdd)
        btnRemove = findViewById(R.id.btnRemove)
        actionButtons = findViewById(R.id.actionButtons)

        recycler.layoutManager = LinearLayoutManager(this)
        prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)

        // ✅ Get selected date
        val date = intent.getStringExtra("DATE") ?: getTodayDate()
        prefsKey = date.replace("/", "_")

        // Decide button visibility
        val isToday = date == getTodayDate()
        val hasData = prefs.getInt(prefsKey, 0) > 0

        actionButtons.visibility =
            if (isToday || hasData) View.VISIBLE else View.GONE

        // Long press ➕ ADD (creates log)
        btnAdd.setOnLongClickListener {
            showWaterDialog(isAdd = true)
            true
        }

        // Long press ➖ REMOVE (NO log)
        btnRemove.setOnLongClickListener {
            showWaterDialog(isAdd = false)
            true
        }

        refreshUI()

        // 🔓 NEW: auto-open Add dialog if coming from today block long-press
        // 🔓 Open SAME add dialog if requested from today block long-press
        val openAddDialog = intent.getBooleanExtra("OPEN_ADD_DIALOG", false)
        if (openAddDialog) {
            showWaterDialog(isAdd = true)
        }

    }


    // 🔄 Refresh total + logs
    private fun refreshUI() {
        val total = prefs.getInt(prefsKey, 0)
        tvWater.text = "$total / $DAILY_GOAL ml"

        val logKey = "${prefsKey}_logs"
        val logsString = prefs.getString(logKey, "") ?: ""

        val logs = mutableListOf<Pair<String, String>>()

        if (logsString.isNotEmpty()) {
            logsString.split(",").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size == 2) {
                    logs.add(Pair(parts[0], parts[1]))
                }
            }
        }

        recycler.adapter = WaterLogAdapter(logs)
    }

    // 💧 Dialog for add/remove
    private fun showWaterDialog(isAdd: Boolean) {
        val options = arrayOf("100 ml", "250 ml", "500 ml", "1 L")
        val values = arrayOf(100, 250, 500, 1000)

        AlertDialog.Builder(this)
            .setTitle(if (isAdd) "Add Water 💧" else "Remove Water 💧")
            .setItems(options) { _, which ->
                updateWater(values[which], isAdd)
            }
            .show()
    }

    // 💾 Update water logic
    private fun updateWater(amount: Int, isAdd: Boolean) {
        val current = prefs.getInt(prefsKey, 0)
        val updated =
            if (isAdd) current + amount
            else maxOf(0, current - amount)

        prefs.edit().putInt(prefsKey, updated).apply()

        // ✅ Add log ONLY when ADD
        if (isAdd) {
            val logKey = "${prefsKey}_logs"
            val time =
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val oldLogs = prefs.getString(logKey, "") ?: ""
            val newLog = "$time|$amount"

            val updatedLogs =
                if (oldLogs.isEmpty()) newLog else "$oldLogs,$newLog"

            prefs.edit().putString(logKey, updatedLogs).apply()
        }

        refreshUI()
    }

    // 📅 Today date
    private fun getTodayDate(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }
}
