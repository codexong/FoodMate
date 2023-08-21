package com.example.foodmate.controller

import com.example.foodmate.model.MeetingDto
import com.example.foodmate.model.MemberDto
import com.example.foodmate.model.MessageDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MeetingController {

    @GET("/getMeetingByNickname")
    fun getMeetingByNickname(
        @Query("nickname") nickname: String
    ): Call<List<MeetingDto>>

    @POST("/insertMeeting")
    fun insertMeeting(
        @Query("boardid") boardid: String,
        @Body meeting: MeetingDto
    ): Call<ResponseBody>

    @GET("/getOneMeeting")
    fun getOneMeeting(@Query("boardid") boardid: String): Call<MeetingDto>

    @GET("/deleteMeeting")
    fun deleteMeeting(@Query("boardid") boardid: String): Call<MeetingDto>

    @POST("/updateMeeting")
    fun updateMeeting(
        @Query("boardid") boardid: String,
        @Body meeting: MeetingDto
    ): Call<ResponseBody>

    @POST("/addMember")
    fun addMember(
        @Query("boardid") boardid: String,
        @Body member: MemberDto
    ): Call<ResponseBody>

    @POST("/removeMember")
    fun removeMember(
        @Query("boardid") boardid: String,
        @Body member: MemberDto
    ): Call<ResponseBody>

    @POST("/addMessage")
    fun addMessage(
        @Query("boardid") boardid: String,
        @Body message: MessageDto
    ): Call<ResponseBody>

}