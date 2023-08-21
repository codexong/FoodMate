package com.example.foodmate.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.MainActivity
import com.example.foodmate.R
import com.example.foodmate.adapter.TodoAdapter
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.controller.TodoController
import com.example.foodmate.databinding.ItemListBinding
import com.example.foodmate.model.TodoDto
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException


class CalendarFragment : Fragment() {
    private val TAG: String = "TodoInsert"

    private lateinit var binding: ItemListBinding
    private lateinit var todoService: TodoController
    private lateinit var memoplus: ImageButton
    private lateinit var titleEditText: EditText
    private lateinit var memoEditText: EditText
    private lateinit var todoAdapter : TodoAdapter
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        memoplus = view.findViewById<ImageButton>(R.id.memoplus)
        todoService = RetrofitBuilder.TodoService()
        binding = ItemListBinding.inflate(layoutInflater)
        // 세션 닉네임 가져오기
        val sessionNicname = SharedPreferencesUtil.getSessionNickname(requireActivity())
        sessionNicname?.let {
            getBoardList(todoService, it) // 게시물 리스트 가져오기
        }

        val isLoggedIn = SharedPreferencesUtil.checkLoggedIn(requireActivity())
        Log.d(TAG, "세션 유지 상태: $isLoggedIn")


        return view
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView = view.findViewById(R.id.recyclerView)

        val todoList: MutableList<TodoDto> = mutableListOf()
        todoAdapter = TodoAdapter(requireContext(), todoList)
        recyclerView.adapter = todoAdapter

        memoplus.setOnClickListener {
            showMemoDialog()
        }
    }


    private fun getBoardList(todoService: TodoController, sessionNicname: String) {
        val boardListCall: Call<List<TodoDto>> = todoService.getMyTodo(sessionNicname)

        boardListCall.enqueue(object : Callback<List<TodoDto>> {
            override fun onResponse(call: Call<List<TodoDto>>, response: Response<List<TodoDto>>) {
                if (response.isSuccessful) {
                    val boardListResponse = response.body()
                    Log.d("lsy", "test: " + boardListResponse)
                    boardListResponse?.let { boardList ->
                        // 세션 닉네임과 동일한 닉네임으로 작성된 게시물 필터링
                        val filteredList = boardList.filter { it.userNicname == sessionNicname }
                        todoAdapter.setData(filteredList)
                    }
                } else {
                    Log.e("TodoBoardList", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<TodoDto>>, t: Throwable) {
                if (t is IOException) {
                    Log.e("TodoList", "Network Error: ${t.message}")
                } else if (t is HttpException) {
                    Log.e("TodoList", "HTTP Error: ${t.code()}")
                } else {
                    Log.e("TodoList", "Error: ${t.message}")
                }
            }
        })
    }

    private fun showMemoDialog() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_memo)

        titleEditText = dialog.findViewById(R.id.titleEditText)
        memoEditText = dialog.findViewById(R.id.memoEditText)
        val saveButton = dialog.findViewById<Button>(R.id.saveButton)
        val memoCancel = dialog.findViewById<Button>(R.id.memoCancel)

        saveButton.setOnClickListener {
            dialog.dismiss()
            sendBoardData()
            val intent = Intent(requireContext(), CalendarFragment::class.java)
        }

        memoCancel.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()
    }


    private fun sendBoardData() {
        val userNicname = SharedPreferencesUtil.getSessionNickname(requireContext()) ?: ""
        val title = titleEditText.text.toString()
        val memo = memoEditText.text.toString()
        val todo = TodoDto(
            title,
            "",
            userNicname,
            memo
        )

        val call = todoService.insertTodo(todo)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")

                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    // 전송 실패한 경우의 처리
                    Toast.makeText(requireContext(), "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e(TAG, "통신 실패: ${t.message}")
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }


}