package com.correct.mobezero.ui.getStarted

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.correct.mobezero.R
import com.correct.mobezero.databinding.FragmentGetStartedBinding
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.onBackPressed
import com.correct.mobezero.helper.setSpannable


class GetStartedFragment : Fragment() {

    private lateinit var binding: FragmentGetStartedBinding
    private lateinit var fragmentListener: FragmentChangeListener

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
        fragmentListener.onFragmentChangedListener(R.id.getStartedFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGetStartedBinding.inflate(inflater,container,false)

        this.onBackPressed {
            requireActivity().finish()
        }

        binding.txtPolicy.setSpannable(startIndex = 40,
            endIndex = 47,
            color = resources.getColor(R.color.alphaBlue,context?.theme)) {
            Log.d("text clicked mohamed", "Service")
            // service url
        }

        binding.txtPolicy.setSpannable(startIndex = 100,
            endIndex = 107,
            color = resources.getColor(R.color.alphaBlue,context?.theme)) {
            Log.d("text clicked mohamed", "Website")
            // website url
        }

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }


        return binding.root
    }
}