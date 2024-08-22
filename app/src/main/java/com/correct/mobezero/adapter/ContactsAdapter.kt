package com.correct.mobezero.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.mobezero.R
import com.correct.mobezero.data.ContactModel
import com.correct.mobezero.helper.ClickListener
import com.correct.mobezero.helper.Constants.CALL
import com.correct.mobezero.helper.Constants.CLICKED
import com.correct.mobezero.helper.Constants.ITEM
import com.correct.mobezero.helper.Constants.NAME
import com.correct.mobezero.helper.Constants.NUMBER
import com.correct.mobezero.helper.generateShortName

class ContactsAdapter(
    private var list: List<ContactModel>,
    private val listener: ClickListener,
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contacts_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_name.text = model.contactName
        holder.txt_number.text = model.contactNumber
        holder.short_name.text = model.contactName.generateShortName()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<ContactModel>) {
        this.list = newList
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_name: TextView = itemView.findViewById(R.id.txt_contact_name)
        val txt_number: TextView = itemView.findViewById(R.id.txt_number)
        val short_name: TextView = itemView.findViewById(R.id.txt_name)
        val call: ImageView = itemView.findViewById(R.id.calling_icon)

        init {
            call.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(NAME, list[bindingAdapterPosition].contactName)
                bundle.putString(NUMBER, list[bindingAdapterPosition].contactNumber)
                bundle.putString(CLICKED, CALL)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED, ITEM)
                bundle.putString(NAME, list[bindingAdapterPosition].contactName)
                bundle.putString(NUMBER, list[bindingAdapterPosition].contactNumber)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnLongClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED, ITEM)
                bundle.putString(NAME, list[bindingAdapterPosition].contactName)
                bundle.putString(NUMBER, list[bindingAdapterPosition].contactNumber)
                listener.onItemLongClickListener(bindingAdapterPosition, bundle)
                true
            }
        }
    }
}