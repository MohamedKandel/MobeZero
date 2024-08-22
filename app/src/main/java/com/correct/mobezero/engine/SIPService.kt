package com.correct.mobezero.engine

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.correct.mobezero.Application
import com.correct.mobezero.helper.UserDefaults
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjmedia_srtp_use
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code

class SIPService : Service(), MyAppObserver {
    private var zem: UserDefaults? = null

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: SIPService
            get() = this@SIPService
    }

    override fun onBind(intent: Intent): IBinder? {
        val action = intent.action
        return if (action != null && action == START_SERVICE) mBinder
        else null
    }

    override fun onCreate() {
        super.onCreate()

        initPjSip()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
    }

    private fun initPjSip() {
        if (app == null) {
            app = MyApp()

            app!!.init(this, filesDir.absolutePath)
        }
        registerAccount()
    }

    fun registerAccount() {
        Log.e("registerAccount", "--registerAccount")
        if (app == null) {
            app = MyApp()
            app!!.init(this, filesDir.absolutePath)
        }

        if (app!!.accList.size == 0) {
            accCfg = AccountConfig()
            configureAccount()
            SipProfile.getInstance().account =
                app!!.addAcc(accCfg)
        } else {
            Log.e("register", "------------------------")
            SipProfile.getInstance().account =
                app!!.accList[0]
            accCfg = SipProfile.getInstance().account.cfg
            configureAccount()
        }
    }

    fun configureAccount() {
        zem = UserDefaults(Application.getInstance().applicationContext)
        val userName = zem!!.userName
        val pass = zem!!.password
        val ip = zem!!.get_SipIp()
        val transport = zem!!.get_sip_transport()
        val proxyIP = zem!!.get_SipProxy()

        Log.e(
            "user details",
            userName + "__" + pass + "__" + ip + "__" + transport + "__" + proxyIP
        )


        val acc_id = userName + "<sip:" + userName + "@" + ip!!.trim { it <= ' ' } + ">"
        val registrar = "sip:" + ip.trim { it <= ' ' }
        val proxy = if (zem!!.get_SipProxy() == "") {
            "sip:" + ip.trim { it <= ' ' } + ";lr;transport=" + zem!!.get_sip_transport()
        } else {
            "sip:" + zem!!.get_SipProxy()!!
                .trim { it <= ' ' } + ";lr;transport=" + zem!!.get_sip_transport()
        }

        Log.e("SIP Proxy : ", proxy)

        accCfg!!.idUri = acc_id
        accCfg!!.regConfig.registrarUri = registrar
        val creds = accCfg!!.sipConfig.authCreds

        creds.clear()
        if (userName!!.length != 0) {
            creds.add(
                AuthCredInfo(
                    "Digest", "*", userName, 0,
                    pass
                )
            )
        }
        val proxies = accCfg!!.sipConfig.proxies
        proxies.clear()
        if (proxy.length != 0) {
            proxies.add(proxy)
        }
        accCfg!!.natConfig.iceEnabled = false
        accCfg!!.videoConfig.autoTransmitOutgoing = true

        setMediaTransport()

        try {
            val info = SipProfile.getInstance().account.info

            Log.e("register", info.regStatusText)
            Log.e("register", info.uri)
        } catch (e: Exception) {
            println(e.message)
        }

        //balance
        //getMinInSec();
    }

    private fun setMediaTransport() {
        if (zem!!.get_sip_transport().equals("udp", ignoreCase = true)) {
            accCfg!!.mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_DISABLED
            accCfg!!.mediaConfig.srtpSecureSignaling = 0
        } else if (zem!!.get_sip_transport().equals("tls", ignoreCase = true)) {
            accCfg!!.mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_MANDATORY
            accCfg!!.mediaConfig.srtpSecureSignaling = 1
        } else if (zem!!.get_sip_transport().equals("tcp", ignoreCase = true)) {
            accCfg!!.mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_DISABLED
            accCfg!!.mediaConfig.srtpSecureSignaling = 0
        }
    }


    override fun notifyIncomingCall(call: MyCall) {
        val prm = CallOpParam()
        if (SipProfile.getInstance().call != null) {
            prm.statusCode = pjsip_status_code.PJSIP_SC_BUSY_HERE
            try {
                call.hangup(prm)
            } catch (e: Exception) {
                call.delete()
                e.printStackTrace()
                return
            }

            return
        }
        /* Answer with ringing */
        prm.statusCode = pjsip_status_code.PJSIP_SC_RINGING
        try {
            call.answer(prm)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        SipProfile.getInstance().showIncomingCall(call, this)
    }

    override fun notifyRegState(code: pjsip_status_code, reason: String, expiration: Int) {
        val msg_str = ""

        if (registerListener != null) {
            registerListener?.notifyRegistrationState(code, reason, expiration, msg_str)
        } else {
            Log.d("OnResume mohamed", "register listener is null")
        }

        if (code == pjsip_status_code.PJSIP_SC_REQUEST_TIMEOUT) {
            registerAccount()
        }
    }


    override fun notifyCallState(call: MyCall) {
        if (SipProfile.getInstance().call == null || call.id != SipProfile.getInstance().call.id) return

        var ci: CallInfo?
        try {
            ci = call.info
        } catch (e: Exception) {
            e.printStackTrace()
            ci = null
        }

        if (callListener != null) {
            callListener!!.notifyCallState(ci)
        }

        if (ci != null && ci.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
            Log.e(this.javaClass.name, "PJSIP_INV_STATE_CONFIRMED")
            SipProfile.getInstance().startTimer()
            try {
                var am: AudioManager? = null
                am =
                    Application.getInstance().applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager

                am.mode = AudioManager.MODE_IN_COMMUNICATION
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (ci != null && ci.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            try {
                Log.e(this.javaClass.name, "PJSIP_INV_STATE_DISCONNECTED")
                SipProfile.getInstance().stopTimer(this)
                SipProfile.getInstance().call = null
                callListener = null
                //balance
            } catch (ex : Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun notifyCallMediaState(call: MyCall) {
    }

    override fun notifyBuddyState(buddy: MyBuddy) {
    }


    override fun notifyChangeNetwork() {
    }

    interface CallListener {
        fun notifyCallState(callInfo: CallInfo?)
    }

    interface OnSipAccountRegisterListener {
        fun notifyRegistrationState(
            code: pjsip_status_code?, reason: String?,
            expiration: Int, status_text: String?
        )
    }

    interface OnBalanceUpdate {
        fun onBalanceUpdate()
    }

    fun setCallListener(listener: CallListener?) {
        callListener = listener
    }

    fun setBalanceListener(listener: OnBalanceUpdate?) {
        balanceListener = listener
    }

    fun removeCallListener() {
        callListener = null
    }

    fun removeBalanceListener() {
        balanceListener = null
    }

    fun setRegisterListener(listener: OnSipAccountRegisterListener?) {
        registerListener = listener
    }

    fun removeRegisterListener() {
        registerListener = null
    }


    /*val balance: Unit
        get() {
            if (!isConnected(this)) {
                return
            }
            val link = zem!!.balanceLink!!.replace("##user##", zem!!.userName!!) + zem!!.userName

            //String link = zem.get_balance_url().replace("##user##", zem.getUserName()) + zem.getUserName();
            Log.e("BalanceLink", link)
            Log.e("BalanceLink", zem!!.userName!!)
            Log.e("BalanceLink", zem!!.password!!)
            Log.e("BalanceLink", zem!!.get_SipIp()!!)
            Log.e("BalanceLink", zem!!.get_SipProxy()!!)


            val queu = Volley.newRequestQueue(this)
            val stringRequest =
                StringRequest(Request.Method.GET, link, object : Response.Listener<String> {
                    override fun onResponse(response: String?) {
                        println(response)
                        if (response != null && response.isNotEmpty()) {
                            if (response.contains("\r")) {
                                response.replace("\r", "")
                            }
                            if (response.contains("\n")) {
                                response.replace("\n", "")
                            }
                            Log.v("response mohamed", response)
                            SipProfile.getInstance().balance = response.trim()
                            if (balanceListener != null) {
                                balanceListener?.onBalanceUpdate()
                            }
                        }
                    }
                }) {
                    it.stackTrace
                }
            queu.add(stringRequest)
        }*/


    /*val minInSec: Unit
        get() {
            if (!isConnected(this)) {
                return
            }

            val link = "" //zem.getMinInSec().replace("##user##", zem.getUserName());

            Log.e("getMinInSec", link)

            val queue = Volley.newRequestQueue(this)

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, link,
                { response ->
                    var response = response
                    if (response != null && !response.equals("", ignoreCase = true)) {
                        if (response.contains("\r")) {
                            response = response.replace("\r", "")
                        }
                        if (response.contains("\n")) {
                            response = response.replace("\n", "")
                        }
                        SipProfile.getInstance().minInSec = response
                    }
                }, { error -> error.printStackTrace() })
            queue.add(stringRequest)
        }*/

    companion object {
        const val START_SERVICE: String = "com.correct.mobezero.engine.START_SERVICE"
        const val START_SERVICE_STICKY: String = "com.correct.mobezero.engine.START_SERVICE_STICKY"

        var app: MyApp? = null
        var accCfg: AccountConfig? = null
        var callListener: CallListener? = null
        var balanceListener: OnBalanceUpdate? = null
        var registerListener: OnSipAccountRegisterListener? = null

        fun createIntent(context: Context?): Intent {
            return Intent(context, SIPService::class.java)
        }
    }
}
