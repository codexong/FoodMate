package com.example.foodmate.controller

import com.example.foodmate.model.LoginResponse
import com.example.foodmate.model.MemberDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MemberController {

    @POST("/insertMember")
    fun insertMember(
        @Query("id") id: String,
        @Query("pw") pw: String,
        @Query("nickname") nickname: String,
        @Query("encodedImage") encodedImage: String
    ): Call<ResponseBody>

    @GET("/getMemberDetail")
    fun getMemberDetail(@Query("id") id: String): Call<MemberDto>

    @FormUrlEncoded
    @POST("/updateMember")
    fun updateMember(
        @Field("id") id: String,
        @Field("pw") pw: String,
        @Field("nickname") nickname: String,
        @Field("encodedImage") encodedImage: String
    ): Call<ResponseBody>

    @GET("/deleteMember")
    fun deleteMember(@Query("id") id: String): Call<String>

    @POST("/login")
    fun login(
        @Body member: MemberDto
    ): Call<LoginResponse>


}