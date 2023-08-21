package com.example.foodmate.controller

import com.example.foodmate.model.BarDto
import retrofit2.Call
import retrofit2.http.GET

interface BarController {
    @GET("/bars")
    fun getAllBars(): Call<List<BarDto>>
}