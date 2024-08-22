package com.correct.mobezero.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.mobezero.R
import com.correct.mobezero.helper.ClickListener
import com.correct.mobezero.helper.Constants
import com.correct.mobezero.helper.Constants.OBJECT
import com.correct.mobezero.helper.generateShortName
import com.correct.mobezero.room.CallLog

class CallLogAdapter(
    private var list: List<CallLog>,
    private val listener: ClickListener,
) : RecyclerView.Adapter<CallLogAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.calls_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallLogAdapter.ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_name.text = model.name
        holder.txt_number.text = model.number
        holder.short_name.text = model.name.generateShortName()
        holder.txt_duration.text = model.duration
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<CallLog>) {
        this.list = newList
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_name: TextView = itemView.findViewById(R.id.txt_contact_name)
        val txt_number: TextView = itemView.findViewById(R.id.txt_number)
        val short_name: TextView = itemView.findViewById(R.id.txt_name)
        val txt_duration: TextView = itemView.findViewById(R.id.txt_duration)

        init {
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(Constants.CLICKED, Constants.ITEM)
                bundle.putParcelable(OBJECT, list[bindingAdapterPosition])
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnLongClickListener {
                val bundle = Bundle()
                bundle.putString(Constants.CLICKED, Constants.ITEM)
                bundle.putParcelable(OBJECT, list[bindingAdapterPosition])
                listener.onItemLongClickListener(bindingAdapterPosition, bundle)
                true
            }
        }
    }
}