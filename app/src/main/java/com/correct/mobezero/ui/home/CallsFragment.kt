package com.correct.mobezero.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.correct.mobezero.MainActivity
import com.correct.mobezero.R
import com.correct.mobezero.adapter.CallLogAdapter
import com.correct.mobezero.databinding.FragmentCallsBinding
import com.correct.mobezero.helper.ClickListener
import com.correct.mobezero.helper.Constants
import com.correct.mobezero.helper.Constants.CLICKED
import com.correct.mobezero.helper.Constants.OBJECT
import com.correct.mobezero.helper.Constants.SWIPED
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.onBackPressed
import com.correct.mobezero.room.CallLog
import com.correct.mobezero.room.CallsDB
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.launch


class CallsFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentCallsBinding
    private lateinit var fragmentListener: FragmentChangeListener
    private lateinit var list: MutableList<CallLog>
    private lateinit var adapter: CallLogAdapter
    private lateinit var callsDB: CallsDB
    private var swipedPosition: Int? = null

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
        fragmentListener.onFragmentChangedListener(R.id.callsFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCallsBinding.inflate(inflater,container,false)
        callsDB = CallsDB.getDBInstance(requireContext())

        list = mutableListOf()
        adapter = CallLogAdapter(list,this)
        binding.recyclerViewHistory.adapter = adapter

        //fillDummyData()
        getDBCalls()

        this.onBackPressed {
            requireActivity().finish()
        }

        binding.headerLayout.btnLogout.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }

        (activity as MainActivity).getBalance {
            val balance = it.replace(Regex("^\\s+"), "")
            Log.d("Current balance main activity", balance)
            binding.headerLayout.txtBalance.text = balance.trim()
        }

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                swipedPosition = viewHolder.bindingAdapterPosition
                val bundle = Bundle()
                bundle.putString(CLICKED, SWIPED)
                bundle.putParcelable(Constants.OBJECT, list[viewHolder.bindingAdapterPosition])
                onItemClickListener(viewHolder.bindingAdapterPosition, bundle)
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(requireContext(), R.color.end_call_color))
                    .addSwipeLeftActionIcon(R.drawable.delete_history_icon)
                    .addSwipeLeftLabel(resources.getString(R.string.delete))
                    .setSwipeLeftLabelColor(Color.rgb(255,255,255))
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)

        return binding.root
    }

    private fun resetSwipedItem() {
        swipedPosition?.let {
            adapter.notifyItemChanged(it)
            swipedPosition = null
        }
    }

    private fun fillDummyData() {
        list.add(
            CallLog(call_ID = 1, name = "Mohamed Hossam", number = "01066168221", date = "12/08/2024",
            time = "4:00 PM", call_type = "Incoming", duration = "2:35 min"))

        list.add(CallLog(call_ID = 2, name = "Ahmed Aly", number = "01225470356", date = "12/08/2024",
            time = "3:10 PM", call_type = "Outgoing", duration = "4:35 min"))

        list.add(CallLog(call_ID = 3, name = "Mazen Mostafa", number = "01577423054", date = "11/08/2024",
            time = "7:00 PM", call_type = "Incoming", duration = "10:35 min"))

        adapter.updateAdapter(list)
    }

    private fun getDBCalls() {
        lifecycleScope.launch {
            list = callsDB.dao().callLog()?.toMutableList()!!
            if (list.isNotEmpty()) {
                Log.i("Current balance main activity", "getDBCalls: ")
                adapter.updateAdapter(list)
            } else {
                Log.i("Current balance main activity", "Dummy")
                fillDummyData()
            }
        }
    }

    override fun onItemClickListener(position: Int, bundle: Bundle?) {
        resetSwipedItem()
        if (bundle != null) {
            val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(OBJECT,CallLog::class.java)
            } else {
                bundle.getParcelable(OBJECT)
            }
            if (model != null) {
                lifecycleScope.launch {
                    callsDB.dao().deleteCall(model.call_ID)
                    Log.i("Item ID", "${model.call_ID}")
                    list.drop(position)
                    list.removeAt(position)
                    adapter.updateAdapter(list)
                }
            }
        }
    }

    override fun onItemLongClickListener(position: Int, bundle: Bundle?) {

    }
}