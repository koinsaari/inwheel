package com.aarokoinsaari.accessibilitymap.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class OverpassResponse(
    val elements: List<Element>
)

@Entity(tableName = "elements")
data class Element(
    @PrimaryKey val id: Long,
    val type: String,
    val lat: Double?,
    val lon: Double?,
    val tags: Tags?
)

data class Tags(
    val amenity: String?,
    val name: String?,
    @SerializedName("addr:city") val city: String?,
    @SerializedName("addr:country") val country: String?,
    @SerializedName("addr:housenumber") val houseNumber: String?,
    @SerializedName("addr:postcode") val postCode: String?,
    @SerializedName("addr:street") val street: String?,
    val brand: String?,
    @SerializedName("opening_hours") val openingHours: String?,
    val wheelchair: String?,
    val phone: String?,
    val email: String?,
    val level: String?,
    val parking: String?,
    val website: String?
)
