package com.correct.mobezero.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.correct.mobezero.MainActivity
import com.correct.mobezero.R
import com.correct.mobezero.adapter.ContactsAdapter
import com.correct.mobezero.data.ContactModel
import com.correct.mobezero.databinding.FragmentContactsBinding
import com.correct.mobezero.helper.ClickListener
import com.correct.mobezero.helper.Constants.CALL
import com.correct.mobezero.helper.Constants.CLICKED
import com.correct.mobezero.helper.Constants.ITEM
import com.correct.mobezero.helper.Constants.NAME
import com.correct.mobezero.helper.Constants.NUMBER
import com.correct.mobezero.helper.Constants.REQUEST_CALL
import com.correct.mobezero.helper.Constants.SWIPED
import com.correct.mobezero.helper.FragmentChangeListener
import com.correct.mobezero.helper.onBackPressed
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator


class ContactsFragment : Fragment(), ClickListener {

    private lateinit var binding: FragmentContactsBinding
    private lateinit var fragmentListener: FragmentChangeListener
    private lateinit var arl: ActivityResultLauncher<String>
    private lateinit var list: MutableList<ContactModel>
    private lateinit var adapter: ContactsAdapter
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
        fragmentListener.onFragmentChangedListener(com.correct.mobezero.R.id.contactsFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        list = mutableListOf()
        adapter = ContactsAdapter(list, this)
        binding.contactsRecyclerView.adapter = adapter

        (activity as MainActivity).getBalance {
            val balance = it.replace(Regex("^\\s+"), "")
            Log.d("Current balance main activity", balance)
            binding.headerLayout.txtBalance.text = balance.trim()
        }

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
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
                bundle.putString(NUMBER, list[viewHolder.bindingAdapterPosition].contactNumber)
                bundle.putString(NAME, list[viewHolder.bindingAdapterPosition].contactName)
                onItemClickListener(viewHolder.bindingAdapterPosition, bundle)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                    .addSwipeRightActionIcon(R.drawable.white_phone_icon)
                    .addSwipeRightLabel(resources.getString(R.string.call))
                    .setSwipeRightLabelColor(Color.rgb(255, 255, 255))
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.contactsRecyclerView)

        arl = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, proceed to list contacts
                listContacts()
            } else {
                // Permission denied
                // Handle the denial, show a message to the user, etc.
            }
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted, list contacts
                listContacts()
            }

            else -> {
                // Request permission
                arl.launch(Manifest.permission.READ_CONTACTS)
            }
        }

        binding.headerLayout.btnLogout.setOnClickListener {
            findNavController().navigate(com.correct.mobezero.R.id.loginFragment)
        }

        this.onBackPressed {
            requireActivity().finish()
        }

        return binding.root
    }

    private fun resetSwipedItem() {
        swipedPosition?.let {
            adapter.notifyItemChanged(it)
            swipedPosition = null
        }
    }

    @SuppressLint("Range")
    private fun listContacts() {
        val contactsList = mutableSetOf<Pair<String, String>>() // List of contact names and numbers

        val contentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                val displayName =
                    it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                // Optionally, get phone numbers associated with the contact
                val hasPhoneNumber =
                    it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                if (hasPhoneNumber > 0) {
                    val phonesCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )

                    phonesCursor?.use { phoneCursor ->
                        while (phoneCursor.moveToNext()) {
                            val phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )
                            if (!contactsList.contains(
                                    Pair(
                                        displayName,
                                        phoneNumber.trim().replace("-", "").replace(" ", "")
                                    )
                                )
                            ) {
                                contactsList.add(
                                    Pair(
                                        displayName,
                                        phoneNumber.trim().replace("-", "").replace(" ", "")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Do something with the contactsList, e.g., display it in a RecyclerView or ListView
        contactsList.forEach { contact ->
            list.add(ContactModel(contact.first, contact.second))
            println("Name: ${contact.first}, Number: ${contact.second}")
        }

        adapter.updateAdapter(list)
    }

    override fun onItemClickListener(position: Int, bundle: Bundle?) {
        if (bundle != null) {
            val clicked = bundle.getString(CLICKED)
            when (clicked) {
                ITEM -> {
                    findNavController().navigate(R.id.dialFragment, bundle)
                }

                CALL -> {

                }

                SWIPED -> {
                    bundle.putBoolean(REQUEST_CALL, true)
                    findNavController().navigate(R.id.dialFragment, bundle)
                    resetSwipedItem()
                }
            }
        }
    }

    override fun onItemLongClickListener(position: Int, bundle: Bundle?) {

    }
}