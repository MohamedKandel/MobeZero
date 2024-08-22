package com.correct.mobezero.room

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Calls")
class CallLog(
    @PrimaryKey
    var call_ID: Int,
    var name: String,
    var number: String,
    var date: String,
    var time: String,
    var call_type: String,
    var duration: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(call_ID)
        parcel.writeString(name)
        parcel.writeString(number)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeString(call_type)
        parcel.writeString(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CallLog> {
        override fun createFromParcel(parcel: Parcel): CallLog {
            return CallLog(parcel)
        }

        override fun newArray(size: Int): Array<CallLog?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this == other)
            return true
        if (javaClass != other?.javaClass) return false

        other as CallLog
        if (call_ID != other.call_ID) return false
        if (name != other.name) return false
        if (number != other.number) return false
        if (date != other.date) return false
        if (time != other.time) return false
        if (call_type != other.call_type) return false
        if (duration != other.duration) return false
        return true
    }

    override fun hashCode(): Int {
        var result = call_ID.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + number.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + call_type.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }
}
