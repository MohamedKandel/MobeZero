package com.correct.mobezero


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.correct.mobezero.databinding.ActivityMainBinding
import com.correct.mobezero.engine.SIPService
import com.correct.mobezero.engine.SipProfile
import com.correct.mobezero.helper.Constants.IS_RETURNED
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.UserDefaults
import com.correct.mobezero.helper.hide
import com.correct.mobezero.helper.show
import com.mkandeel.networkconnectivity.ConnectionManager
import com.mkandeel.networkconnectivity.OnConnectionChangedListener


class MainActivity : AppCompatActivity(), FragmentChangeListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var userDefaults: UserDefaults
    private lateinit var connectivity: ConnectionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        userDefaults = UserDefaults(this)

        setContentView(binding.root)

        if (Application.getInstance().service != null && userDefaults.get_SipIp()?.isNotEmpty() == true) {
            Application.getInstance().service.registerAccount()

        }

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT


        val navhost = supportFragmentManager.findFragmentById(R.id.nav_host)
        navController = navhost?.let { NavHostFragment.findNavController(it) }!!

        intent?.let {
            val isReturned = it.getBooleanExtra(IS_RETURNED,false)
            if (isReturned) {
                navController.navigate(R.id.dialFragment)
                binding.btmNavView.selectedItemId = R.id.placeholder
            }
        }

        binding.btmNavView.background = null
        setupWithNavController(binding.btmNavView, navController)
        binding.btmNavView.menu.getItem(1).isEnabled = false

        binding.btmNavView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.placeholder -> {
                    Log.d("Item clicked mohamed", "Dial")
//                    navigate to dial fragment
                    navController.navigate(R.id.dialFragment)
                }

                R.id.contacts_item -> {
                    Log.d("Item clicked mohamed", "Contact")
//                    navigate to contact fragment
                    navController.navigate(R.id.contactsFragment)
                }

                R.id.calls_item -> {
                    Log.d("Item clicked mohamed", "Call log")
//                    navigate to call log fragment
                    navController.navigate(R.id.callsFragment)
                }
            }

            true
        }

        binding.fab.setOnClickListener {
            Log.d("Item clicked mohamed", "Dial")
            navController.navigate(R.id.dialFragment)
            binding.btmNavView.selectedItemId = R.id.placeholder
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

    fun getBalance(function: (balance: String)-> Unit) {
        connectivity = ConnectionManager(this)
        connectivity.listenConnection(this, object : OnConnectionChangedListener {
            override fun onConnectionAvailable() {
                val link = userDefaults.balanceLink?.replace("##user##", userDefaults.userName!!) + userDefaults.userName

                //String link = zem.get_balance_url().replace("##user##", zem.getUserName()) + zem.getUserName();
                Log.e("BalanceLink", link)
                Log.e("BalanceLink", userDefaults.userName!!)
                Log.e("BalanceLink", userDefaults.password!!)
                Log.e("BalanceLink", userDefaults.get_SipIp()!!)
                Log.e("BalanceLink", userDefaults.get_SipProxy()!!)

                val queu = Volley.newRequestQueue(this@MainActivity)
                val stringRequest =
                    StringRequest(Request.Method.GET, link,
                        { response ->
                            println(response)
                            if (!response.isNullOrEmpty()) {
                                if (response.contains("\r")) {
                                    response.replace("\r","")
                                }
                                if (response.contains("\n")) {
                                    response.replace("\n","")
                                }
                                Log.v("response mohamed", response)
                                SipProfile.getInstance().balance = response.trim()
                                if (SIPService.balanceListener != null) {
                                    SIPService.balanceListener?.onBalanceUpdate()
                                }
                                function(response.toString())
                            }
                        }) {
                        it.stackTrace
                    }
                queu.add(stringRequest)
            }

            override fun onConnectionLosing() {
                return
            }

            override fun onConnectionLost() {
                return
            }

            override fun onConnectionUnAvailable() {
                return
            }
        })
    }

    override fun onFragmentChangedListener(fragmentID: Int) {
        when (fragmentID) {
            R.id.splashFragment -> {
                binding.bottomAppBar.hide()
                binding.fab.hide()
            }

            R.id.getStartedFragment -> {
                binding.bottomAppBar.hide()
                binding.fab.hide()
            }

            R.id.loginFragment -> {
                binding.bottomAppBar.hide()
                binding.fab.hide()
            }

            R.id.dialFragment -> {
                binding.bottomAppBar.show()
                binding.fab.show()
                binding.btmNavView.selectedItemId = R.id.placeholder
            }
            R.id.contactsFragment -> {
                binding.bottomAppBar.show()
                binding.fab.show()
            }
            R.id.callsFragment -> {
                binding.bottomAppBar.show()
                binding.fab.show()
            }
        }
    }
}