package com.example.foodmate.model

import com.google.gson.annotations.SerializedName

data class BarDto(

    @SerializedName("main_TITLE")
    val main_TITLE: String,

    @SerializedName("main_IMG_NORMAL")
    val main_IMG_NORMAL: String,

    @SerializedName("addr1")
    val addr1: String
)