package com.correct.mobezero.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class UserDefaults(context: Context) {
    //private var _prefs: SharedPreferences? = null
    //private var _editor: SharedPreferences.Editor? = null


    private lateinit var _prefs: SharedPreferences
    private lateinit var _editor: SharedPreferences.Editor

    init {
        _prefs = PreferenceManager.getDefaultSharedPreferences(context)
        _editor = _prefs.edit()
    }

    var opcode: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("opcode", field)
            return field
        }
        set(opcode) {
            if (this._editor == null) {
                return
            }

            _editor.putString("opcode", opcode)
        }
    var userName: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("userName", field)
            return field
        }
        set(name) {
            if (this._editor == null) {
                return
            }

            _editor.putString("userName", name)
        }
    var password: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("password", field)
            return field
        }
        set(password) {
            if (this._editor == null) {
                return
            }

            _editor.putString("password", password)
        }
    var server: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("server", field)
            return field
        }
        set(server) {
            if (this._editor == null) {
                return
            }

            _editor.putString("server", server)
        }
    private val sipServer = ""
    private val sipProxy = ""
    private var du_server: String? = ""
    var header: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("header", field)
            return field
        }
        set(header) {
            if (this._editor == null) {
                return
            }

            _editor.putString("header", header)
        }
    var footer: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("footer", field)
            return field
        }
        set(footer) {
            if (this._editor == null) {
                return
            }

            _editor.putString("footer", footer)
        }
    var ivr: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("ivr", field)
            return field
        }
        set(ivr) {
            if (this._editor == null) {
                return
            }

            _editor.putString("ivr", ivr)
        }
    var balanceLink: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("balanceLink", field)
            return field
        }
        set(balanceLink) {
            if (this._editor == null) {
                return
            }

            _editor.putString("balanceLink", balanceLink)
        }
    private val pTime = ""
    private val minInSec = ""
    var lastDialedNumber: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("lastDialedNumber", field)
            return field
        }
        set(lastDialedNumber) {
            if (this._editor == null) {
                return
            }

            _editor.putString("lastDialedNumber", lastDialedNumber)
        }
    var configURL: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("configURLV2", field)
            return field
        }
        set(configURL) {
            if (this._editor == null) {
                return
            }

            _editor.putString("configURLV2", configURL)
        }
    var encryptionKey: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("encryptionKey", field)
            return field
        }
        set(encryptionKey) {
            if (this._editor == null) {
                return
            }

            _editor.putString("encryptionKey", encryptionKey)
        }
    var isAccountCreated: Boolean = false
        get() {
            if (this._prefs == null) {
                return false
            }

            field = _prefs.getBoolean("isAccountCreated", field)
            return field
        }
        set(accountCreated) {
            if (this._editor == null) {
                return
            }

            _editor.putBoolean("isAccountCreated", accountCreated)
        }

    //vpn
    var vpnUserName: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("vpnUserName", field)
            return field
        }
        set(vpnUserName) {
            if (this._editor == null) {
                return
            }

            _editor.putString("vpnUserName", vpnUserName)
        }
    var vpnPassword: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("vpnPassword", field)
            return field
        }
        set(vpnPassword) {
            if (this._editor == null) {
                return
            }

            _editor.putString("vpnPassword", vpnPassword)
        }
    var vpnServerEtisalat: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("vpnServerEtisalat", field)
            return field
        }
        set(vpnServerEtisalat) {
            if (this._editor == null) {
                return
            }

            _editor.putString("vpnServerEtisalat", vpnServerEtisalat)
        }
    var vpnServerDu: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }

            field = _prefs.getString("vpnServerDu", field)
            return field
        }
        set(vpnServerDu) {
            if (this._editor == null) {
                return
            }

            _editor.putString("vpnServerDu", vpnServerDu)
        }
    private var get_api_ip: String? = ""
    private var get_SipProxy: String? = ""
    private var set_pTime: String? = ""
    private var get_sip_transport: String? = ""
    private var get_sip_proxy_port: String? = ""
    private var set_SipIp: String? = ""
    private var getIsShowAds = 0

    private var _transfer_url: String? = ""
    private var _recharge_url: String? = ""
    private var _rate_url: String? = ""
    private var _login_url: String? = ""
    private var _config_url: String? = ""
    private var _balance_url: String? = ""
    private val _accountNumber = ""
    var callerID: String? = ""
        get() {
            if (this._prefs == null) {
                return ""
            }
            field = _prefs.getString("callerID", field)
            return field
        }
        set(callerID) {
            if (this._editor == null) {
                return
            }
            _editor.putString("callerID", callerID)
        }


    init {
        this._prefs = context.getSharedPreferences("PREFS_PRIVATE", Context.MODE_PRIVATE)
        this._editor = _prefs.edit()
    }


    var duServer: String?
        get() {
            if (this._prefs == null) {
                return ""
            }

            this.du_server = _prefs.getString("du_server", du_server)
            return this.du_server
        }
        set(du_server) {
            if (this._editor == null) {
                return
            }

            _editor.putString("du_server", du_server)
        }


    fun setLoggedIn(isLoggedIn: Boolean) {
        if (this._editor == null) {
            return
        }

        _editor.putBoolean("isLoggedIn", isLoggedIn)
    }

    fun get_api_ip(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.get_api_ip = _prefs.getString("get_api_ip", get_api_ip)
        return this.get_api_ip
    }

    fun set_api_ip(get_api_ip: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("get_api_ip", get_api_ip)
    }


    fun set_pTime(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.set_pTime = _prefs.getString("set_pTime", set_pTime)
        return this.set_pTime
    }

    fun set_pTime(set_pTime: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("set_pTime", set_pTime)
    }


    fun get_SipProxy(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.get_SipProxy =
            _prefs.getString("get_SipProxy", get_SipProxy)
        return this.get_SipProxy
    }

    fun set_SipProxy(get_SipProxy: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("get_SipProxy", get_SipProxy)
    }


    fun get_sip_transport(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.get_sip_transport =
            _prefs.getString("get_sip_transport", get_sip_transport)
        return this.get_sip_transport
    }

    fun set_sip_transport(get_sip_transport: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("get_sip_transport", get_sip_transport)
    }

    fun get_sip_proxy_port(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.get_sip_proxy_port =
            _prefs.getString("get_sip_proxy_port", get_sip_proxy_port)
        return this.get_sip_proxy_port
    }

    fun set_sip_proxy_port(get_sip_proxy_port: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("get_sip_proxy_port", get_sip_proxy_port)
    }

    var isShowAds: Int?
        get() {
            if (this._prefs == null) {
                return 0
            }

            this.getIsShowAds = _prefs.getInt("getIsShowAds", getIsShowAds)
            return this.getIsShowAds
        }
        set(getIsShowAds) {
            if (this._editor == null) {
                return
            }

            _editor.putInt("getIsShowAds", getIsShowAds!!)
        }

    fun get_SipIp(): String? {
        if (this._prefs == null) {
            return ""
        }

        this.set_SipIp = _prefs.getString("set_SipIp", set_SipIp)
        return this.set_SipIp
    }

    fun set_SipIp(set_SipIp: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("set_SipIp", set_SipIp)
    }


    fun get_recharge_url(): String? {
        if (this._prefs == null) {
            return ""
        }

        this._recharge_url =
            _prefs.getString("_recharge_url", _recharge_url)
        return this._recharge_url
    }

    fun se_recharge_url(_recharge_url: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("_recharge_url", _recharge_url)
        this.save()
    }

    fun get_rate_url(): String? {
        if (this._prefs == null) {
            return ""
        }

        this._rate_url = _prefs.getString("_rate_url", _rate_url)
        return this._rate_url
    }

    fun set_rate_url(_rate_url: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("_rate_url", _rate_url)
    }

    fun get_transfer_url(): String? {
        if (this._prefs == null) {
            return ""
        }

        this._transfer_url =
            _prefs.getString("_transfer_url", _transfer_url)
        return this._transfer_url
    }

    fun set_transfer_url(pass: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("_transfer_url", pass)
    }

    fun get_login_url(): String? {
        if (this._prefs == null) {
            return ""
        }

        this._login_url = _prefs.getString("_login_url", _login_url)
        return this._transfer_url
    }

    fun set_login_url(_login_url: String?) {
        if (this._editor == null) {
            return
        }

        _editor.putString("_login_url", _login_url)
    }

    fun get_config_url(): String? {
        if (this._prefs == null) {
            return ""
        }

        this._config_url = _prefs.getString("_config_url", _config_url)
        return this._config_url
    }

    fun set_config_url(_config_url: String?) {
        if (this._editor == null) {
            return
        }
        _editor.putString("_config_url", _config_url)
    }


    fun get_balance_url(): String? {
        if (this._prefs == null) {
            return ""
        }
        this._balance_url =
            _prefs.getString("_balance_url", _balance_url)
        return this._balance_url
    }

    fun set_balance_url(_balance_url: String?) {
        if (this._editor == null) {
            return
        }
        _editor.putString("_balance_url", _balance_url)
    }

    /* For save to ZemSettings */
    fun save() {
        if (this._editor == null) {
            return
        }
        _editor.commit()
    }
}