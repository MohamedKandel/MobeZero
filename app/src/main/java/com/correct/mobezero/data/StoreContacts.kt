package com.correct.mobezero.data

import java.io.Serializable

/**
 * Created by admin on 7/2/17.
 */
class StoreContacts : Serializable {
    var contactName: String? = null
    var contactNumber: String? = null
    var photoUri: String? = null
    var largePhotoUri: String? = null
    var phoneContactID: Int = 0
    var contactID: Long = 0
}
