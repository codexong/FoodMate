package com.example.foodmate

import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.network.RetrofitBuilder
import com.example.foodmate.UpdateActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeleteMemberController {

    private val apiService = RetrofitBuilder.MemberService()

    fun deleteMember(
        activity: UpdateActivity,
        id: String,
        password: String,
        nickname: String,
        callback: (Boolean) -> Unit
    ) {
        apiService.deleteMember(id).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    val deleteResponse = response.body()
                    if (deleteResponse != null && deleteResponse == "success") {
                        // 회원 삭제
                        SharedPreferencesUtil.clearSession(activity)
                        callback.invoke(true)
                    } else {
                        callback.invoke(false)
                    }
                } else {
                    callback.invoke(false)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                callback.invoke(false)
            }
        })
    }
}