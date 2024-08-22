package com.correct.mobezero.ui

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.mobezero.R
import com.correct.mobezero.databinding.FragmentSplashBinding
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.UserDefaults
import com.correct.mobezero.helper.onBackPressed

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private lateinit var listener: FragmentChangeListener
    private var countDownTimer: CountDownTimer? = null
    private var milliFinished: Long = 0
    private val TAG = "Splash screen mohamed"
    private lateinit var prefs: UserDefaults

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentChangeListener) {
            listener = context
        } else {
            throw ClassCastException("Super class doesn't implement interface class")
        }
    }

    override fun onResume() {
        super.onResume()
        listener.onFragmentChangedListener(R.id.splashFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater,container,false)
        prefs = UserDefaults(requireContext())

        binding.root.keepScreenOn = true
        val animation = AnimationUtils.loadAnimation(requireContext(),R.anim.scale)

        animation.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.circle.clearAnimation()
            }

            override fun onAnimationRepeat(p0: Animation?) {

            }
        })
        binding.circle.startAnimation(animation)

        this.onBackPressed {
            requireActivity().finish()
        }

        startSplash()

        return binding.root
    }

    private fun startSplash() {
        countDownTimer = object : CountDownTimer(1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                milliFinished = millisUntilFinished
                Log.i(TAG, "onTick: millisUntilFinished=$millisUntilFinished")
            }

            override fun onFinish() {
                // navigate to onBoarding
                Log.i(TAG, "onFinish: ${milliFinished}")
                // check for users if found navigate to home otherwise navigate to login
                if (prefs.userName.isNullOrEmpty()) {
                    findNavController().navigate(R.id.getStartedFragment)
                } else {
                    findNavController().navigate(R.id.dialFragment)
                }
            }
        }.start()
    }

    override fun onDetach() {
        super.onDetach()
        countDownTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }

}