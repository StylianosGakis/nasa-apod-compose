package com.stylianosgakis.composenasapotd.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class NasaPhoto(
    @PrimaryKey(autoGenerate = false)
    val date: String,
    val title: String,
    @SerializedName("media_type")
    val mediaType: String,
    val explanation: String,
    @SerializedName("service_version")
    val serviceVersion: String? = null,
    val url: String,
    @SerializedName("hdurl")
    val hdUrl: String? = null,
    val copyright: String? = null
) : Parcelable {
    fun isImage(): Boolean = mediaType == "image"
}
