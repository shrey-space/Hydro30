package com.example.hydro30

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional: fullscreen behind status bar
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)

        // 🚀 If challenge already active → skip MainActivity
        if (prefs.getBoolean("challenge_active", false)) {
            val startDate = prefs.getString("challenge_start_date", null)

            val intent = Intent(this, ChallengeActivity::class.java)
            intent.putExtra("START_DATE", startDate)
            startActivity(intent)
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        val greetingText = findViewById<TextView>(R.id.tvGreeting)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val acceptButton = findViewById<Button>(R.id.btnAccept)

        // ✨ Animate greeting
        animateName(greetingText)

        // ➡️ Start challenge
        acceptButton.setOnClickListener {
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1
            val year = datePicker.year

            val startDate = "$day/$month/$year"

            // ✅ SAVE CHALLENGE STATE
            val prefs = getSharedPreferences("water_prefs", MODE_PRIVATE)
            prefs.edit()
                .putBoolean("challenge_active", true)
                .putString("challenge_start_date", startDate)
                .apply()

            val intent = Intent(this, ChallengeActivity::class.java)
            intent.putExtra("START_DATE", startDate)
            startActivity(intent)
            finish() // 👈 important
        }

    }

    // ✨ Text animation
    private fun animateName(textView: TextView) {
        val handler = Handler(Looper.getMainLooper())
        val prefix = "Hi "
        val name = "Shreyas 👋"
        var index = 0

        textView.text = prefix

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index < name.length) {
                    textView.text = prefix + name.substring(0, index + 1)
                    index++
                    handler.postDelayed(this, 150)
                }
            }
        }, 300)
    }
}
