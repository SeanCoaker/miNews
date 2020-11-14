package com.coaker.newsaggregatorapp.ui.keywords

import android.os.Parcel
import android.os.Parcelable

class Keyword() : Parcelable {
    var word: String? = null
    var isNotifier: Boolean? = null

    constructor(parcel: Parcel) : this() {
        word = parcel.readString()
        isNotifier = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(word)
        parcel.writeByte(if (isNotifier!!) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Keyword> {
        override fun createFromParcel(parcel: Parcel): Keyword {
            return Keyword(parcel)
        }

        override fun newArray(size: Int): Array<Keyword?> {
            return arrayOfNulls(size)
        }
    }
}