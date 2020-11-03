package com.coaker.newsaggregatorapp

import android.os.Parcel
import android.os.Parcelable

class NewsData() : Parcelable {
    var source: String? = null
    var author: String? = null
    var title: String? = null
    var description: String? = null
    var url: String? = null
    var urlToImage: String? = null
    var publishedAt: String? = null
    var content: String? = null

    constructor(parcel: Parcel) : this() {
        source = parcel.readString()
        author = parcel.readString()
        title = parcel.readString()
        description = parcel.readString()
        url = parcel.readString()
        urlToImage = parcel.readString()
        publishedAt = parcel.readString()
        content = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(source)
        parcel.writeString(author)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(url)
        parcel.writeString(urlToImage)
        parcel.writeString(publishedAt)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewsData> {
        override fun createFromParcel(parcel: Parcel): NewsData {
            return NewsData(parcel)
        }

        override fun newArray(size: Int): Array<NewsData?> {
            return arrayOfNulls(size)
        }
    }


}