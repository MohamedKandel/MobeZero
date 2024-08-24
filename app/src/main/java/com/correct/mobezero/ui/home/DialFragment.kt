package com.correct.mobezero.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.mobezero.Application
import com.correct.mobezero.MainActivity
import com.correct.mobezero.R
import com.correct.mobezero.databinding.FragmentDialBinding
import com.correct.mobezero.engine.SIPService
import com.correct.mobezero.engine.SipProfile
import com.correct.mobezero.helper.Constants.NUMBER
import com.correct.mobezero.helper.Constants.REQUEST_CALL
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.RegisterListener
import com.correct.mobezero.helper.UserDefaults
import com.correct.mobezero.helper.backSpace
import com.correct.mobezero.helper.onBackPressed
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.pjsip_status_code


class DialFragment : Fragment(), SIPService.OnSipAccountRegisterListener, RegisterListener {

    private lateinit var binding: FragmentDialBinding
    private lateinit var fragmentListener: FragmentChangeListener
    private lateinit var userDefaults: UserDefaults
    private lateinit var arl: ActivityResultLauncher<Array<String>>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Activity doesn't implement this interface")
        }
    }

    override fun onResume() {
        super.onResume()
        SipProfile.getInstance().setRegisterListener(this)
        SipProfile.getInstance().getStatusText(requireActivity())
        Application.getInstance().service.setRegisterListener(this)
        fragmentListener.onFragmentChangedListener(R.id.dialFragment)
        if (Application.getInstance() == null) {
            Log.v("OnResume mohamed", "Application null")
        } else {
            Log.v("OnResume mohamed", "Application not null")
        }
        if (Application.getInstance()?.service != null) {
            Log.v("OnResume mohamed", "Service not null")
        } else {
            Log.v("OnResume mohamed", "Service null")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDialBinding.inflate(inflater, container, false)
        userDefaults = UserDefaults(requireContext())

        binding.headerLayout.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        binding.txtNumber.setTextIsSelectable(true)
        binding.txtNumber.setOnTouchListener { v, event ->
            v.onTouchEvent(event)
            val imm = v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
            true
        }

        binding.txtName.text = userDefaults.header
        binding.txtLink.text = userDefaults.footer

        binding.iconAccStatus.tag = resources.getString(R.string.acct_registering)
//        getBalance()

        (activity as MainActivity).getBalance {
            val balance = it.replace(Regex("^\\s+"), "")
            Log.d("Current balance main activity", balance)
            binding.headerLayout.txtBalance.text = balance.trim()
        }

        if (arguments != null) {
            val number = requireArguments().getString(NUMBER) ?: ""
            val validNumber = if (number.startsWith("20") || number.startsWith("+20")) {
                if (number.startsWith("+20")) {
                    number.replace("+","")
                } else {
                    number
                }
            } else {
                "2$number"
            }
            if (number.isNotEmpty()) {
                binding.txtNumber.setText(validNumber)
            }
            val request_call = requireArguments().getBoolean(REQUEST_CALL,false)
            if (request_call) {
                makeCall(validNumber)
            }
        }

        val url =
            if (userDefaults.footer?.contains("http") == false || userDefaults.footer?.contains("https") == false) {
                "https://${userDefaults.footer}"
            } else {
                userDefaults.footer
            }

        binding.txtLink.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(url)
            )
            requireActivity().startActivity(intent)
        }

        this.onBackPressed {
            requireActivity().finish()
        }

        binding.apply {
            txtOne.setOnClickListener {
                addText("1")
            }
            txtTwo.setOnClickListener {
                addText("2")
            }
            txtThree.setOnClickListener {
                addText("3")
            }
            txtFour.setOnClickListener {
                addText("4")
            }
            txtFive.setOnClickListener {
                addText("5")
            }
            txtSix.setOnClickListener {
                addText("6")
            }
            txtSeven.setOnClickListener {
                addText("7")
            }
            txtEight.setOnClickListener {
                addText("8")
            }
            txtNine.setOnClickListener {
                addText("9")
            }
            txtZero.setOnClickListener {
                addText("0")
            }
            txtHash.setOnClickListener {
                addText("#")
            }
            txtZero.setOnLongClickListener {
                addText("+")
                true
            }
            txtAst.setOnClickListener {
                addText("*")
            }
            deleteIcon.setOnClickListener {
                binding.txtNumber.backSpace()
            }
            deleteIcon.setOnLongClickListener {
                binding.txtNumber.setText("")
                true
            }
        }

        binding.iconAccStatus.setOnClickListener {
            if (binding.iconAccStatus.tag == resources.getString(R.string.acct_registered)) {
                makeCall(binding.txtNumber.text.toString())
            }
        }

        binding.btnCall.setOnClickListener {
            makeCall(binding.txtNumber.text.toString())
        }

        return binding.root
    }

    private fun checkAndRequestPermission(phoneNumber: String) {
        val readContactPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val recordAudioPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (readContactPermissionGranted && recordAudioPermissionGranted) {
            // Both permissions are granted, do something
            makeCall(phoneNumber)
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

    private fun makeCall(phoneNumber: String) {
        arl = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {permissions ->
            val readContactPermissionGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false
            val recordAudioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            if (readContactPermissionGranted && recordAudioPermissionGranted) {
                if (!userDefaults.lastDialedNumber.equals("") && binding.txtNumber.text.toString()
                        .isEmpty()
                ) {
                    binding.txtNumber.setText(userDefaults.lastDialedNumber)
                } else {
                    if (binding.txtNumber.text.toString().isNotEmpty()) {
                        if (SipProfile.getInstance().call != null) {
                            Log.e("register", "Call exist")
                            val prm2 = CallOpParam()
                            prm2.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                            try {
                                SipProfile.getInstance().call.hangup(prm2)
                                val target = "sip:${
                                    phoneNumber.trim()
                                }@${userDefaults.get_SipIp()}"

                                Log.v("target caller", target)
                                SipProfile.getInstance()
                                    .makeCall(target, requireActivity())
                                userDefaults.lastDialedNumber =
                                    phoneNumber.trim()
                                userDefaults.save()
                                Log.v("register", "try block")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Log.v("register", "catch block")
                                Log.e("register", "onGranted: ", e)
                            }
                        } else {
                            val target = "sip:${
                                binding.txtNumber.text.toString().trim()
                            }@${userDefaults.get_SipIp()}"
                            Log.v("target caller", target)
                            SipProfile.getInstance().makeCall(target, requireActivity())
                            userDefaults.lastDialedNumber =
                                binding.txtNumber.text.toString().trim()
                            userDefaults.save()
                        }
                    } else {
                        Toast.makeText(activity, "No number to dial", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                if (!readContactPermissionGranted) {
                    // Camera permission denied
                    Toast.makeText(requireContext(), "Read Contact permission denied", Toast.LENGTH_SHORT).show()
                }
                if (!recordAudioPermissionGranted) {
                    // Location permission denied
                    Toast.makeText(requireContext(), "Record Audio permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        checkAndRequestPermission(phoneNumber)
    }

    private fun addText(number: String) {
        val position = binding.txtNumber.selectionStart
        binding.txtNumber.text.insert(position,number)
    }

    override fun onPause() {
        super.onPause()
        SipProfile.getInstance().removeRegisterListener()
        if (Application.getInstance().service != null) {
//            Application.getInstance()!!.service!!.setRegisterListener(null)
            Application.getInstance().service.removeRegisterListener()
        } else {
            Log.d("onResume mohamed", "Service is null")
        }
    }

    override fun notifyRegistrationState(
        code: pjsip_status_code?,
        reason: String?,
        expiration: Int,
        status_text: String?
    ) {

    }

    override fun onAccountRegisterListener(status: String) {
        Log.d("Account status mohamed", status)
        if (status == resources.getString(R.string.acct_registered)) {
            binding.iconAccStatus.setImageResource(R.drawable.ready_icon)
            binding.iconAccStatus.tag = resources.getString(R.string.acct_registered)
            binding.btnCall.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.green, requireContext().theme)
            )
            binding.btnCall.isEnabled = true
        } else {
            binding.iconAccStatus.setImageResource(R.drawable.un_ready_icon)
            binding.iconAccStatus.tag = resources.getString(R.string.acct_registering)
            binding.btnCall.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.un_ready_color, requireContext().theme)
            )
            binding.btnCall.isEnabled = false
        }

    }
}