package com.correct.mobezero.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.correct.mobezero.Application
import com.correct.mobezero.MainActivity
import com.correct.mobezero.R
import com.correct.mobezero.databinding.ActivityCallBinding
import com.correct.mobezero.engine.CallService
import com.correct.mobezero.engine.MyCall
import com.correct.mobezero.engine.SIPService
import com.correct.mobezero.engine.SipProfile
import com.correct.mobezero.helper.CallStatusListener
import com.correct.mobezero.helper.Constants.IS_RETURNED
import com.correct.mobezero.helper.backSpace
import com.correct.mobezero.helper.displayDialog
import com.correct.mobezero.utils.ConstrainedDragAndDropView
import com.correct.mobezero.utils.ContactUtils
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import java.util.regex.Pattern

class CallActivity : AppCompatActivity(), SIPService.CallListener, SensorEventListener,
    CallStatusListener {

    private lateinit var binding: ActivityCallBinding
    private lateinit var mSensorManager: SensorManager
    private lateinit var mSensor: Sensor
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: WakeLock
    private var field = 0x00000020
    private lateinit var shake: Animation
    private lateinit var audio: AudioManager
    private lateinit var handler_: Handler
    private var lastCallState: String = "Calling.."
    private val timer = Timer()
    private lateinit var task: TimerTask
    private var time = 0.0
    private lateinit var lastCallInfo: CallInfo
    private var isSpeakerOn: Boolean = false
    private var enableMute: Boolean = false
    private var isHoldOn: Boolean = false
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var call: MyCall

    private lateinit var arl: ActivityResultLauncher<Array<String>>

    var mLocalBroadcastManager: LocalBroadcastManager? = null
    var mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "com.durga.action.close") {
                endCall()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)

        setContentView(binding.root)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT

        audio = getSystemService(AUDIO_SERVICE) as AudioManager
        getAudioPermission()

        val number = SipProfile.getInstance().callingNumber

        if (SipProfile.getInstance().isInComingCall && !SipProfile.getInstance().isCallOngoing) {
            setupIncomingCall()
        }

        if (number != null) {
            UpdateUI(number)
        }

        updateCallInfo()

        binding.btnEnd.setOnClickListener {
            endCall()
        }

        binding.btnSpeaker.setOnClickListener {
            enableSpeakerPhone()
        }

        binding.btnHold.setOnClickListener {
            enableHold()
        }

        binding.btnMute.setOnClickListener {
            enableMuteButton()
        }

        binding.btnDial.setOnClickListener {
            /*val call_ID = if (SipProfile.getInstance().call.id != null) {
                SipProfile.getInstance().call.id
            } else {
                0
            }*/
            shoDialedPad()
        }

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction("com.durga.action.close")
        mLocalBroadcastManager!!.registerReceiver(mBroadcastReceiver, mIntentFilter)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun shoDialedPad() {
        val dialog = this.displayDialog(R.layout.keyboard)
        val txtNumber = dialog.findViewById<EditText>(R.id.txt_number)
        val delete = dialog.findViewById<ImageView>(R.id.delete_icon)
        val txt_0 = dialog.findViewById<TextView>(R.id.txt_number_0)
        val txt_1 = dialog.findViewById<TextView>(R.id.txt_number_1)
        val txt_2 = dialog.findViewById<TextView>(R.id.txt_number_2)
        val txt_3 = dialog.findViewById<TextView>(R.id.txt_number_3)
        val txt_4 = dialog.findViewById<TextView>(R.id.txt_number_4)
        val txt_5 = dialog.findViewById<TextView>(R.id.txt_number_5)
        val txt_6 = dialog.findViewById<TextView>(R.id.txt_number_6)
        val txt_7 = dialog.findViewById<TextView>(R.id.txt_number_7)
        val txt_8 = dialog.findViewById<TextView>(R.id.txt_number_8)
        val txt_9 = dialog.findViewById<TextView>(R.id.txt_number_9)
        val txt_ast = dialog.findViewById<TextView>(R.id.txt_ast)
        val txt_hash = dialog.findViewById<TextView>(R.id.txt_hash)

        txt_0.setOnClickListener {
            txtNumber.addText("0")
        }

        txt_1.setOnClickListener {
            txtNumber.addText("1")
        }

        txt_2.setOnClickListener {
            txtNumber.addText("2")
        }

        txt_3.setOnClickListener {
            txtNumber.addText("3")
        }

        txt_4.setOnClickListener {
            txtNumber.addText("4")
        }

        txt_5.setOnClickListener {
            txtNumber.addText("5")
        }

        txt_6.setOnClickListener {
            txtNumber.addText("6")
        }

        txt_7.setOnClickListener {
            txtNumber.addText("7")
        }

        txt_8.setOnClickListener {
            txtNumber.addText("8")
        }

        txt_9.setOnClickListener {
            txtNumber.addText("9")
        }

        txt_hash.setOnClickListener {
            txtNumber.addText("#")
        }

        txt_ast.setOnClickListener {
            txtNumber.addText("*")
        }

        txt_0.setOnLongClickListener {
            txtNumber.addText("+")
            true
        }

        txtNumber.setTextIsSelectable(true)
        txtNumber.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val imm = v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            true
        }

        delete.setOnClickListener {
            txtNumber.backSpace()
        }
    }

    private fun EditText.addText(number: String) {
        val position = this.selectionStart
        this.text.insert(position,number)
        SipProfile.getInstance().sendDTMF(number)
    }

    private fun setupIncomingCall() {
        binding.dragDropLayout.setVisibility(View.VISIBLE)
        binding.scrollView.setVisibility(View.GONE)

        binding.rippleBackground.startRippleAnimation()
        shake = AnimationUtils.loadAnimation(this, R.anim.shakeanim)
        binding.imageAccept.startAnimation(shake)

        binding.dragDropLayout.setDragHandle(findViewById(R.id.image_accept))
        binding.dragDropLayout.addDropTarget(findViewById(R.id.container_middle))
        binding.dragDropLayout.addDropTarget(findViewById(R.id.container_right))
        binding.dragDropLayout.addDropTarget(findViewById(R.id.container_left))


        binding.dragDropLayout.setDropListener(object : ConstrainedDragAndDropView.DropListener {
            override fun onDrop(dropIndex: Int, dropTarget: View?) {
                // center
                if (dropIndex == 0) {
                    binding.imageAccept.setBackgroundResource(R.drawable.btn_normal)
                } else if (dropIndex == 1) {
                    binding.imageAccept.clearAnimation()
                    acceptCall()
                } else if (dropIndex == 2) {
                    binding.imageAccept.clearAnimation()
                    rejectCall()
                }

                //Toast.makeText(CallActivity.this, "Dropped on Target " + dropIndex, Toast.LENGTH_SHORT).show();
            }
        })

        playRingTone()
    }

    private fun playRingTone() {
        if (!SipProfile.getInstance().isInComingCall) {
            audio.isSpeakerphoneOn = false
        } else {
            mMediaPlayer = MediaPlayer()
            val PERMISSIONS =
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (hasPermissions(this, *PERMISSIONS)) {
                val alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                try {
                    mMediaPlayer.setDataSource(this, alert)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                try {
                    val uri =
                        Uri.parse("android.resource://" + packageName + "/" + R.raw.class_notification)
                    mMediaPlayer.setDataSource(this, uri)
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }

            if (audio.getStreamVolume(AudioManager.STREAM_RING) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)
                mMediaPlayer.isLooping = true
                try {
                    mMediaPlayer.prepare()
                    mMediaPlayer.start()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun checkAndRequestPermission() {
        val readContactPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val recordAudioPermissionGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (readContactPermissionGranted && recordAudioPermissionGranted) {
            // Both permissions are granted, do something
            acceptCall()
        } else {
            // Request the permissions that are not granted
            val permissionsToRequest = mutableListOf<String>()
            if (!readContactPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
            }
            if (!recordAudioPermissionGranted) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            }
            arl.launch(permissionsToRequest.toTypedArray())
        }
    }

    fun acceptCall() {
        arl = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {permissions ->
            val readContactPermissionGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
            val recordAudioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            if(recordAudioPermissionGranted && readContactPermissionGranted) {
                //do your task.
                val prm = CallOpParam()
                try {
                    val ci = SipProfile.getInstance().call.info
                } catch (e: java.lang.Exception) {
                    Log.e("calling mohamed", "Error", e)
                }
                prm.statusCode = pjsip_status_code.PJSIP_SC_OK
                try {
                    SipProfile.getInstance().isCallOngoing = true
                    SipProfile.getInstance().call.answer(prm)
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying) {
                        mMediaPlayer.stop()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                binding.dragDropLayout.setVisibility(View.GONE)
                binding.scrollView.setVisibility(View.VISIBLE)
            } else {
                rejectCall()
            }
        }
        checkAndRequestPermission()
        /*Permissions.check(
            this, arrayOf<String>(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.RECORD_AUDIO
            ), null, null,
            object : PermissionHandler() {
                override fun onGranted() {
                    //do your task.
                    val prm = CallOpParam()
                    try {
                        val ci = SipProfile.getInstance().call.info
                    } catch (e: java.lang.Exception) {
                        Log.e("calling mohamed", "Error", e)
                    }
                    prm.statusCode = pjsip_status_code.PJSIP_SC_OK
                    try {
                        SipProfile.getInstance().isCallOngoing = true
                        SipProfile.getInstance().call.answer(prm)
                        if (mMediaPlayer != null && mMediaPlayer.isPlaying) {
                            mMediaPlayer.stop()
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    binding.dragDropLayout.setVisibility(View.GONE)
                    binding.scrollView.setVisibility(View.VISIBLE)
                }

                override fun onDenied(context: Context, deniedPermissions: ArrayList<String>) {
                    rejectCall()
                }
            })*/
    }

    fun rejectCall() {
        audio.isMicrophoneMute = false
        audio.isSpeakerphoneOn = false
        if (SipProfile.getInstance().call != null) {
            val prm2 = CallOpParam()
            prm2.statusCode = pjsip_status_code.PJSIP_SC_BUSY_HERE
            try {
                SipProfile.getInstance().call.hangup(prm2)
                SipProfile.getInstance().isCallOngoing = false
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                Toast.makeText(applicationContext, "Please let me permission", Toast.LENGTH_SHORT)
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    2
                )
            }
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        permission!!
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun UpdateUI(dialedNumber: String) {
        try {
            var calle_phonenumber = formatPhoneNumber(dialedNumber)

            if (calle_phonenumber.length > 0) {
                if (calle_phonenumber[0] == 'z') {
                    calle_phonenumber = calle_phonenumber.substring(1, calle_phonenumber.length)
                }
            }
            binding.txtNumber.setText(calle_phonenumber)
            val PERMISSIONS = arrayOf(Manifest.permission.READ_CONTACTS)
            var phnContactName = ""
            //binding.txtCallerName.setText("Mohamed Hossam")
            if (hasPermissions(this, *PERMISSIONS)) {
                phnContactName = ContactUtils.getName(this,calle_phonenumber).toString()
                if (phnContactName != null && phnContactName != "") {
                    binding.txtCallerName.setText(phnContactName)
                } else {
                    binding.txtCallerName.setText("")
                }
            } else {
                binding.txtCallerName.setText("")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun enableProximitySensor() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        try {
            // Yeah, this is hidden field.
            field = PowerManager::class.java.getField(
                "PROXIMITY_SCREEN_OFF_WAKE_LOCK"
            ).getInt(null)
        } catch (ignored: Throwable) {
        }

        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(field, localClassName)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        registerSensor()
    }

    fun endCall() {
        audio.setMicrophoneMute(false)
        audio.setSpeakerphoneOn(false)

        if (isMyServiceRunning(CallService::class.java)) {
            stopService(CallService.createIntent(this))
        }

        SipProfile.getInstance().setUserNotDisconnectedYet(false)
        if (SipProfile.getInstance().call != null) {
            val prm2 = CallOpParam()
            prm2.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
            try {
                SipProfile.getInstance().call.hangup(prm2)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        finish()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateCallInfo() {
        if (SipProfile.getInstance().call != null) {
            try {
                lastCallInfo = SipProfile.getInstance().call.info
                updateCallState(lastCallInfo)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        } else {
            try {
                if (this::lastCallInfo.isInitialized) {
                    updateCallState(lastCallInfo)
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCallState(ci: CallInfo) {
        var call_state = ""
        try {
            if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED || ci.lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TERMINATED || ci.lastStatusCode == pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT || ci.lastStatusCode == pjsip_status_code.PJSIP_SC_NOT_ACCEPTABLE_HERE) {
                audio.isMicrophoneMute = false
                audio.isSpeakerphoneOn = false
                if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_DECLINE) {
                    // disconnected by user code 603
                    call_state = getString(R.string.call_disconnected_by_caller)
                } else if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_OK) {
                    // disconnected by other side code 200
                    call_state = getString(R.string.call_disconnected_by_other_side)
                } else if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_NOT_FOUND) {
                    // disconnected by other side code 404
                    call_state = getString(R.string.not_found)
                    //binding.txtCallStatus.text = call_state
                } else if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_INTERNAL_SERVER_ERROR) {
                    // disconnected by other side code 500
                    call_state = "internal server error"
                    //binding.txtCallStatus.text = call_state
                } else if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_PAYMENT_REQUIRED) {
                    call_state = "Payment Required"
                    //binding.txtCallStatus.text = call_state
                    Thread.sleep(1000)
                } else {
                    call_state = "call disconnected..Please Wait"
                    //binding.txtCallStatus.text = call_state
                }

                binding.txtCallStatus.text = call_state
                Log.v("Call state mohamed", call_state)

                SipProfile.getInstance().isCallOngoing = false
                //SymProfile.getInstance().setCall(null);
                if (isMyServiceRunning(CallService::class.java)) {
                    stopService(CallService.createIntent(this))
                }

                finish()
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CALLING) {
                call_state = getString(R.string.call_in_calling_state)
                binding.txtCallStatus.text = call_state
                Log.v("calling mohamed", "calling 1")
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                // start timer
                //startTimer();
                Log.v("calling mohamed", "answered 1")
                timerStatus(true)
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                // stop timer
                //stopTimer();
                Log.v("calling mohamed", "ended 1")
                timerStatus(false)
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_PROGRESS) {
                    call_state = getString(R.string.call_in_calling_state)
                    Log.v("calling mohamed", "calling 2")
                    binding.txtCallStatus.text = call_state
                } else if (ci.lastStatusCode == pjsip_status_code.PJSIP_SC_RINGING) {
                    call_state = getString(R.string.call_ringing_state)
                    Log.v("calling mohamed", "ringing")
                    binding.txtCallStatus.text = call_state
                } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                    // start timer
                    //startTimer();
                    Log.v("calling mohamed", "answered 2")
                    timerStatus(true)
                } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                    // stop timer
                    //stopTimer();
                    Log.v("calling mohamed", "ended 2")
                    timerStatus(false)
                }
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
                // connecting code 200
                call_state = getString(R.string.call_in_connecting_state)
                Log.v("calling mohamed", "connecting")
                binding.txtCallStatus.text = call_state
            } else if (ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                // confirmed code 200
                call_state = getString(R.string.call_in_confirmed_state)
                Log.v("calling mohamed", "call connected")
                binding.txtCallStatus.text = call_state
//                containerCallControl.setVisibility(View.VISIBLE)
//                declineBtn.setVisibility(View.VISIBLE)
//                containerAccept.setVisibility(View.GONE)
            } else {
                Log.v("calling mohamed", "another state")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        if (call_state.length > 0) {
            lastCallState = call_state
        } else {
            binding.txtCallStatus.text = lastCallState
        }
    }

    private fun timerStatus(start: Boolean) {
        if (start) {
            task = object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        time++
                        binding.txtCallStatus.text = getTimerText()
                    }
                }
            }
            timer.schedule(task, 0, 1000)
        } else {
            if (task != null) {
                task.cancel()
            }
        }
    }

    private fun getTimerText(): String {
        val rounded = Math.round(time).toInt()
        val seconds = ((rounded % 86400) % 3600) % 60
        val minutes = ((rounded % 86400) % 3600) / 60
        val hours = ((rounded % 86400) / 3600)

        return formatTime(seconds, minutes, hours)
    }

    @SuppressLint("DefaultLocale")
    private fun formatTime(seconds: Int, minutes: Int, hours: Int): String {
        return String.format("%02d", hours) + ":" + String.format(
            "%02d",
            minutes
        ) + ":" + String.format("%02d", seconds)
    }

    private fun formatPhoneNumber(phonenumber: String): String {
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

    fun unregisterSensor() {
        mSensorManager.unregisterListener(this)
    }

    fun registerSensor() {
        mSensorManager.registerListener(
            this, mSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun enableHold() {
        if (!isHoldOn) {
            SipProfile.getInstance().holdCall()
            binding.btnHold.setImageResource(R.drawable.resume_call_icon)
            binding.txtHold.text = resources.getString(R.string.un_hold)
//            val drawableTop = resources.getDrawable(R.mipmap.ic_hold_off)
//            callHoldButton.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
            isHoldOn = true
        } else {
            SipProfile.getInstance().unHoldCall()
            binding.btnHold.setImageResource(R.drawable.hold_icon)
            binding.txtHold.text = resources.getString(R.string.hold)
//            val drawableTop = resources.getDrawable(R.mipmap.ic_hold)
//            callHoldButton.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
            isHoldOn = false
        }
    }

    private fun enableSpeakerPhone() {
        if (!isSpeakerOn) {
            audio.mode = AudioManager.MODE_IN_COMMUNICATION
            binding.btnSpeaker.setImageResource(R.drawable.speaker_off_icon)
            binding.txtSpeaker.text = resources.getString(R.string.speaker)
            audio.isSpeakerphoneOn = true
            isSpeakerOn = true
        } else {
            audio.mode = AudioManager.MODE_IN_COMMUNICATION
            binding.btnSpeaker.setImageResource(R.drawable.speaker_icon)
            binding.txtSpeaker.text = resources.getString(R.string.speaker)
            audio.isSpeakerphoneOn = false
            isSpeakerOn = false
        }
    }

    //control mute option
    private fun enableMuteButton() {
        if (!enableMute) {
            audio.isMicrophoneMute = true
            binding.btnMute.setImageResource(R.drawable.un_mute_icon)
            binding.txtMute.text = resources.getString(R.string.un_mute)
//            val drawableTop = resources.getDrawable(R.mipmap.ic_mute_on)
//            muteButton.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
            enableMute = true
        } else {
            audio.isMicrophoneMute = false
            enableMute = false
            binding.btnMute.setImageResource(R.drawable.mute_icon)
            binding.txtMute.text = resources.getString(R.string.mute)
//            val drawableTop = resources.getDrawable(R.mipmap.ic_mute)
//            muteButton.setCompoundDrawablesWithIntrinsicBounds(null, drawableTop, null, null)
        }
    }

    override fun notifyCallState(callInfo: CallInfo?) {
        try {
            if (callInfo!!.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                Log.v("calling mohamed", "answered 3")
            } else if (callInfo.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                Log.v("calling mohamed", "ended 3")
            }
            lastCallInfo = callInfo
            this@CallActivity.runOnUiThread { updateCallState(lastCallInfo) }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        println("Distance of face" + (p0?.values?.get(0) ?: 0f))
        if (this::lastCallInfo.isInitialized) {
            if (lastCallInfo == null) {
                unregisterSensor()
                return
            }

            if (p0?.values?.get(0) == 0f) {
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire()
                }
            } else {
                if (wakeLock.isHeld()) {
                    wakeLock.release()
                }
            }
        } else {
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra(IS_RETURNED, true)
            startActivity(intent)
            finish()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onResume() {
        super.onResume()

        SipProfile.getInstance().setCallListener(this)

        if (Application.getInstance().service != null) {
            Application.getInstance().service.setCallListener(this)
        }
        enableProximitySensor()
    }

    override fun onPause() {
        super.onPause()
        super.onPause()
        SipProfile.getInstance().cancelListener()
        if (Application.getInstance().service != null) {
            Application.getInstance().service.setCallListener(null)
        }
        unregisterSensor()
    }

    override fun onCallInfoChanged(ci: String) {
        //lastCallInfo = ci
        Log.i("Call status onCallChanged mohamed", ci)
        //updateCallState(lastCallInfo)
    }
}