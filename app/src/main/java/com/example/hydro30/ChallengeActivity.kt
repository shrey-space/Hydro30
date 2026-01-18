package com.example.hydro30

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class ChallengeActivity : AppCompatActivity() {

    // ✅ make recyclerView accessible for refresh
    private lateinit var recyclerView: RecyclerView

    private val DAILY_GOAL = 2500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_challenge)

        recyclerView = findViewById(R.id.recyclerDays)

        // ✅ Fixed grid so all 30 fit nicely
        val layoutManager = object : GridLayoutManager(this, 4) {
            override fun canScrollVertically(): Boolean = false
        }
        recyclerView.layoutManager = layoutManager

        val startDateStr = intent.getStringExtra("START_DATE") ?: return

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(startDateStr)!!

        val daysList = mutableListOf<String>()
        for (i in 0 until 30) {
            daysList.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        recyclerView.adapter = DayAdapter(daysList)

        val btnCancel = findViewById<ImageView>(R.id.btnCancel)
        btnCancel.setOnClickListener {
            showCancelConfirmation()
        }
    }

    // 🔄 Refresh UI when returning from TodayWaterActivity
    override fun onResume() {
        super.onResume()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    /* ===============================
       🟢 ADD WATER DIALOG (NEW)
       =============================== */
    fun showAddWaterDialog() {
        val options = arrayOf("100 ml", "250 ml", "500 ml", "1 L")
        val values = arrayOf(100, 250, 500, 1000)

        AlertDialog.Builder(this)
            .setTitle("Add Water 💧")
            .setItems(options) { _, which ->
                addWater(values[which])
            }
            .show()
    }

    /* ===============================
       💧 ADD WATER LOGIC (NEW)
       =============================== */
    private fun addWater(amount: Int) {
        val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)

        val todayKey =
            SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())

        val current = prefs.getInt(todayKey, 0)
        val updated = current + amount
        prefs.edit().putInt(todayKey, updated).apply()

        // ✅ Save log with time
        val time =
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val logKey = "${todayKey}_logs"
        val oldLogs = prefs.getString(logKey, "") ?: ""
        val newLog = "$time|$amount"

        val updatedLogs =
            if (oldLogs.isEmpty()) newLog else "$oldLogs,$newLog"

        prefs.edit().putString(logKey, updatedLogs).apply()

        // 🎊 Confetti if goal reached
        if (current < DAILY_GOAL && updated >= DAILY_GOAL) {
            showConfetti()
        }

        // 🔄 Update grid immediately
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Challenge")
            .setMessage("Are you sure you want to cancel this challenge? All progress will be deleted.")
            .setPositiveButton("Confirm") { _, _ ->
                clearAllData()
                goToMain()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // 🎊 CONFETTI ANIMATION
    fun showConfetti() {
        val container = findViewById<FrameLayout>(R.id.confettiContainer)
        container.visibility = View.VISIBLE
        container.removeAllViews()

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        repeat(40) {
            val confetti = View(this)

            confetti.setBackgroundColor(
                if (Random.nextBoolean())
                    Color.parseColor("#42A5F5")
                else
                    Color.parseColor("#66BB6A")
            )

            val size = Random.nextInt(12, 24)
            val params = FrameLayout.LayoutParams(size, size)
            params.leftMargin = Random.nextInt(screenWidth)
            params.topMargin = -size
            confetti.layoutParams = params

            container.addView(confetti)

            val animator = ValueAnimator.ofFloat(0f, screenHeight.toFloat())
            animator.duration = Random.nextLong(3000, 5000)
            animator.interpolator = LinearInterpolator()

            animator.addUpdateListener {
                confetti.translationY = it.animatedValue as Float
            }

            animator.start()
        }

        container.postDelayed({
            container.removeAllViews()
            container.visibility = View.GONE
        }, 5000)
    }
}
