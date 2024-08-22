package com.correct.mobezero.ui.getStarted

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.correct.mobezero.R
import com.correct.mobezero.databinding.FragmentLoginBinding
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.UserDefaults
import com.correct.mobezero.helper.onBackPressed
import com.google.android.material.textfield.TextInputLayout


class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var fragmentListener: FragmentChangeListener
    private lateinit var viewModel: AuthViewModel
    private lateinit var userDefaults: UserDefaults

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            fragmentListener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }

    override fun onResume() {
        super.onResume()
        fragmentListener.onFragmentChangedListener(R.id.loginFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        userDefaults = UserDefaults(requireContext())


        binding.txtCode.setText(userDefaults.opcode)
        binding.txtName.setText(userDefaults.userName)
        binding.txtPassword.setText(userDefaults.password)
        binding.txtCallerId.setText(userDefaults.callerID)

        this.onBackPressed {
            findNavController().navigate(R.id.getStartedFragment)
        }

        binding.txtCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty()) {
                    binding.codeLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    binding.codeLayout.endIconDrawable =
                        ContextCompat.getDrawable(requireContext(), R.drawable.check_icon)
                    binding.codeLayout.setEndIconTintList(null) // Remove the tint
                } else {
                    binding.codeLayout.endIconMode = TextInputLayout.END_ICON_NONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.txtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty()) {
                    binding.usernameLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    binding.usernameLayout.endIconDrawable =
                        ContextCompat.getDrawable(requireContext(), R.drawable.check_icon)
                    binding.usernameLayout.setEndIconTintList(null) // Remove the tint
                } else {
                    binding.usernameLayout.endIconMode = TextInputLayout.END_ICON_NONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.txtCallerId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!p0.isNullOrEmpty()) {
                    binding.callerIdLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    binding.callerIdLayout.endIconDrawable =
                        ContextCompat.getDrawable(requireContext(), R.drawable.check_icon)
                    binding.callerIdLayout.setEndIconTintList(null) // Remove the tint
                } else {
                    binding.callerIdLayout.endIconMode = TextInputLayout.END_ICON_NONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.getStartedFragment)
        }

        binding.btnLogin.setOnClickListener {
            if (binding.txtCode.text.toString().isEmpty() || binding.txtName.text.toString().isEmpty() ||
                binding.txtPassword.text.toString().isEmpty()) {
                Toast.makeText(requireContext(),resources.getString(R.string.fill_required),Toast.LENGTH_SHORT).show()
            } else {
                val code = binding.txtCode.text.toString()
                login(code)
            }
        }

        return binding.root
    }

    private fun login(code: String) {
        viewModel.login(code)
        viewModel.loginResponse.observe(viewLifecycleOwner) {
            // success
            if (it.response_code == 1) {
                val transport = "udp"
                val proxyIP = it.proxy_ip
                val ipPort = proxyIP.split(":")
                userDefaults.userName = binding.txtName.text.toString()
                userDefaults.password = binding.txtPassword.text.toString()
                userDefaults.opcode = binding.txtCode.text.toString()

                userDefaults.set_SipIp(it.register.trim())
                userDefaults.header = it.header.trim()
                userDefaults.footer = it.footer.trim()
                userDefaults.ivr = it.ivr.trim()
                userDefaults.set_api_ip(it.cdr.trim())
                Log.e("set_api_ip", it.cdr.trim())
                userDefaults.set_api_ip("204.12.216.210:5222")
                userDefaults.balanceLink = it.balance_link.trim()
                Log.e("balance link mohamed", "My balance link is " + userDefaults.balanceLink)
                userDefaults.set_pTime(it.pkey.trim())
                userDefaults.set_SipProxy(proxyIP.trim())
                userDefaults.set_sip_transport(transport.trim())
                userDefaults.set_sip_proxy_port(ipPort[1].trim())
                userDefaults.encryptionKey = it.proxy_encryption.trim()
                userDefaults.isShowAds = Integer.parseInt(it.show_advertisement.trim())
                userDefaults.save()
                findNavController().navigate(R.id.dialFragment)
            } else {
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
            }
        }
    }
}