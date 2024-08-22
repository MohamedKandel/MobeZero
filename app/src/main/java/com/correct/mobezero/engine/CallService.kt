package com.correct.mobezero.engine

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import com.correct.mobezero.R
import com.correct.mobezero.ui.CallActivity
import java.util.regex.Pattern

/**
 * Created by USER on 2/13/18.
 */
public class CallService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("CallService------", "onStartCommand")
        return START_STICKY
    }

    val NOTIFY_ID = 1

    override fun onCreate() {
        super.onCreate()
        val CHANNEL_ID = "my_channel_01"
        var callmode = ""
        callmode = if (SipProfile.getInstance().isInComingCall) {
            "Incoming call.."
        } else {
            "Dialed call.."
        }
        Log.e("CallService------", "Pending intent creating")

        val notIntent = Intent(this, CallActivity::class.java)
        //notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        val pendInt = PendingIntent.getActivity(
            this, 0,
            notIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val builder = Notification.Builder(this)

        builder.setContentIntent(pendInt)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker(getResources().getText(R.string.app_name))
            .setOngoing(true)
            .setContentTitle(formatPhoneNumber(SipProfile.getInstance().callingNumber))
            .setContentText(callmode)
        val not = builder.build()


        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence =
                getString(R.string.app_name) // The user-visible name of the channel.
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)
            builder.setSmallIcon(R.mipmap.ic_launcher)
            builder.setColor(getResources().getColor(R.color.white))
            builder.setChannelId(CHANNEL_ID)
        }

        startForeground(NOTIFY_ID, not)

        val intent = Intent(this, CallActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    fun formatPhoneNumber(phonenumber: String): String {
        var phonenumber = phonenumber
        val sipUriSpliter = Pattern
            .compile("^(?:\")?([^<\"]*)(?:\")?[ ]*(?:<)?sip(?:s)?:([^@]*)@[^>]*(?:>)?")
        val m = sipUriSpliter.matcher(phonenumber)
        if (m.matches()) {
            if (!TextUtils.isEmpty(m.group(2))) {
                phonenumber = m.group(2)
            } else if (!TextUtils.isEmpty(m.group(1))) {
                phonenumber = m.group(1) // change by zem
            }
        }

        return phonenumber
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    companion object {
        fun createIntent(context: Context?): Intent {
            return Intent(context, CallService::class.java)
        }
    }
}
