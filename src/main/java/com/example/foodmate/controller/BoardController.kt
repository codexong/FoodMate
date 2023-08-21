package com.example.foodmate.controller

import com.example.foodmate.model.BoardDto
import com.example.foodmate.model.MemberDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardController {

    @POST("/insertBoard")
    fun insertBoard(@Body board: BoardDto): Call<ResponseBody>

    @GET("/boardList")
    fun getAllBoard(): Call<List<BoardDto>>

    @GET("/getBoardDetail")
    fun getBoardDetail(
        @Query("boardid") id: String): Call<BoardDto>

    @GET("/myBoard")
    fun getMyBoard(@Query("userNicname") userNicname: String): Call<List<BoardDto>>

    @GET("/deleteBoard")
    fun deleteBoard(
        @Query("boardid") boardid: String
    ): Call<ResponseBody>

    @POST("/updateBoard")
    fun updateBoard(
        @Query("boardid") boardId: String,
        @Body board: BoardDto
    ): Call<ResponseBody>
}