package com.example.yourway.post

import android.os.Parcel
import android.os.Parcelable

data class Post(
    var title: String = "",
    var description: String = "",
    var imageUrls: MutableList<String> = mutableListOf()
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title);
        dest.writeString(description);
    }
}