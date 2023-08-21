package com.example.foodmate.controller

import com.example.foodmate.model.TodoDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TodoController {

    @POST("/insertTodo")
    fun insertTodo(@Body todo: TodoDto): Call<ResponseBody>

    @GET("/todoList")
    fun getAllTodo(): Call<List<TodoDto>>

    @GET("/myTodo")
    fun getMyTodo(@Query("userNicname") userNicname: String): Call<List<TodoDto>>

    @GET("/deleteTodo")
    fun deleteTodo(
        @Query("todoid") todoid: String
    ): Call<ResponseBody>
}