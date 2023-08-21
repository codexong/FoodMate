package com.example.foodmate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.foodmate.controller.BoardController
import com.example.foodmate.databinding.ActivityBoardDeleteBinding
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardDelete : AppCompatActivity() {

    private lateinit var binding: ActivityBoardDeleteBinding
    private lateinit var boardService: BoardController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardService = RetrofitBuilder.BoardService()

        val boardId = intent.getStringExtra("boardId")

        if (boardId != null) {
            deleteBoard(boardId)
        } else {
            Log.e("BoardDelete", "Error: Board ID is null")
        }
    }

    private fun deleteBoard(boardId: String) {
        val deleteBoardCall: Call<ResponseBody> = boardService.deleteBoard(boardId)

        deleteBoardCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Board deleted successfully
                    // Handle the response, e.g., display a success message
                    Log.d("BoardDelete", "Board deleted successfully")

                    val intent = Intent(this@BoardDelete, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.e("BoardDelete", "Error: ${response.code()}")
                    // Handle the error response, e.g., display an error message
                }
                finish() // Finish the BoardDelete activity after deletion
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("BoardDelete", "Error: ${t.message}")
                // Handle the failure, e.g., display an error message
                finish() // Finish the BoardDelete activity after failure
            }
        })
    }
}
