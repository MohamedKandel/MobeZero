package com.correct.mobezero.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import android.util.Log
import com.correct.mobezero.data.StoreContacts

class ContactUtils(val context: Context) {
    val allContacts: ArrayList<StoreContacts>
        @SuppressLint("Range")
        get() {
            val startnow = SystemClock.uptimeMillis()
            val arrContacts: ArrayList<StoreContacts> = ArrayList<StoreContacts>()

            val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val selection: String = ContactsContract.Contacts.HAS_PHONE_NUMBER

            val projection = arrayOf<String>(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
            )

            var cursor: Cursor? = context.getContentResolver().query(
                uri,
                projection,
                selection, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            var count = 0
            try {
                cursor!!.moveToFirst()
                while (cursor.isAfterLast == false) {
                    count++
                    val contactNumber =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val contactName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val phoneContactID =
                        cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID))
                    val contactID =
                        cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)).toLong()

                    //Log.e("con ", "name " + contactName + " " + " PhoeContactID " + phoneContactID + "  ContactID " + contactID);
                    //Log.e("con photo uri", "+++++++++"+getThumbnailImage(contactID));
                    val sc: StoreContacts = StoreContacts()
                    sc.contactName = contactName
                    sc.contactNumber = contactNumber
                    sc.contactID = contactID
                    sc.phoneContactID = phoneContactID
                    sc.photoUri = cursor.getString(5)
                    sc.largePhotoUri = cursor.getString(6)

                    arrContacts.add(sc)
                    cursor.moveToNext()
                }
                cursor.close()
                cursor = null
            } catch (e: Exception) {
                e.printStackTrace()
            }


            val endnow = SystemClock.uptimeMillis()
            Log.e(
                "END",
                "TimeForContacts " + (endnow - startnow) + " ms--" + "total contacts fetched----" + count
            )
            return arrContacts
        }

    companion object {
        //Fetch contact display name
        fun getName(context: Context, phoneNumber: String) : String? {
            val contentResolver = context.contentResolver
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME
            )

            var contactName: String? = null

            val cursor = contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    contactName = it.getString(nameIndex)
                }
            }
            return contactName
        }
    }
}
