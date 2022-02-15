package edu.washington.awty

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    lateinit var start : Button
    lateinit var stop : Button
    var started: Boolean = false
    lateinit var pendingIntent: PendingIntent

    class IntentListener : BroadcastReceiver() {
        init {
            Log.i("IntentListener", "Intent listener created")
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            val message = intent?.extras?.getString("EXTRA_MESSAGE")
            val phone = intent?.extras?.getString("EXTRA_PHONE")
            Log.i("IntentListener", "We received an intent: $message : $phone")
            Toast.makeText(context?.getApplicationContext(), "${phone}:${message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val numberTxt = findViewById<EditText>(R.id.number)
        val messageTxt = findViewById<EditText>(R.id.message)
        val minutesTxt = findViewById<EditText>(R.id.minutes)

        val receiver = IntentListener()
        val intFilter = IntentFilter()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        // listening for intents
        intFilter.addAction("edu.us.ischool.NAG")
        registerReceiver(receiver, intFilter)

        start = findViewById<Button>(R.id.start)
        stop = findViewById<Button>(R.id.stop)

        stop.isEnabled = false

        start.setOnClickListener {
            var number = numberTxt.text.toString()
            var message = messageTxt.text.toString()
            var minutes = minutesTxt.text.toString()
            stop.isEnabled = true
            start.isEnabled = false
            if (alarmManager != null && checkEmpty(number, message, minutes) && isNumber(minutes)) {
                sendMessages(alarmManager, message, number, minutes.toInt())
            }
        }

        stop.setOnClickListener {
            started = false
            stop.isEnabled = false
            start.isEnabled = true
            if (alarmManager != null) {
                stopMessages(alarmManager)
            }
        }
    }

    private fun stopMessages(alarm: AlarmManager) {
        alarm.cancel(pendingIntent)

        started = false
    }

    private fun isNumber(s: String): Boolean {
        return try {
            s.toInt()
            true
        } catch (ex: NumberFormatException) {
            Toast.makeText(this, "Invalid number or number of minutes entered", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun sendMessages(alarm: AlarmManager, message: String, phone: String, min: Int) {
        val intent = Intent("edu.us.ischool.NAG")
        var time = System.currentTimeMillis() + (min * 60000)
        intent.putExtra("EXTRA_MESSAGE", message)
        intent.putExtra("EXTRA_PHONE", phone)

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, time, (min * 60000).toLong(), pendingIntent)

        started = true
    }

    private fun checkEmpty(number: String, message: String, minutes: String): Boolean {
        if (number.toInt() < 999999999 || number.length !== 10) {
            Toast.makeText(this, "Invalid number entered", Toast.LENGTH_SHORT).show()
            return false
        } else if (message.length === 0) {
            Toast.makeText(this, "No message entered", Toast.LENGTH_SHORT).show()
            return false
        } else if (minutes.toInt() < 1) {
            Toast.makeText(this, "Invalid number of minutes entered", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
