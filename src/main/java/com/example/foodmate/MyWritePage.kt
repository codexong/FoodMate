package com.example.foodmate


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.Util.MainActivityUtil
import com.example.foodmate.adapter.BoardAdapter
import com.example.foodmate.controller.BoardController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityMyWritePageBinding
import com.example.foodmate.model.BoardDto
import com.example.foodmate.network.RetrofitBuilder
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class MyWritePage : AppCompatActivity() {
    private val TAG: String = "MyWritePage"

    private lateinit var binding: ActivityMyWritePageBinding
    private lateinit var boardService: BoardController

    private lateinit var recyclerView: RecyclerView
    private lateinit var boardAdapter: BoardAdapter

    private lateinit var menu: Menu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyWritePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardService = RetrofitBuilder.BoardService()
        recyclerView = findViewById(R.id.mainLayout)
        recyclerView.layoutManager = LinearLayoutManager(this@MyWritePage)

        val boardList: MutableList<BoardDto> = mutableListOf()
        boardAdapter = BoardAdapter(this@MyWritePage, boardList)
        recyclerView.adapter = boardAdapter


        // 세션 닉네임 가져오기
        val sessionNicname = SharedPreferencesUtil.getSessionNickname(this)
        sessionNicname?.let {
            getBoardList(boardService, it) // 게시물 리스트 가져오기
        }


        val isLoggedIn = SharedPreferencesUtil.checkLoggedIn(this)
        Log.d(TAG, "세션 유지 상태: $isLoggedIn")

        //메인 유틸 코드
        MainActivityUtil.initViews(this@MyWritePage)
        val plusButton = findViewById<ImageButton>(R.id.plus)
        plusButton.setOnClickListener {
            MainActivityUtil.showPopupMenu(this, plusButton)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fragmentManager = supportFragmentManager
        val mainLayout = findViewById<View>(R.id.mainLayout)
        MainActivityUtil.setBottomNavigationListener(bottomNavigationView, fragmentManager,mainLayout)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainActivityUtil.onOptionsItemSelected(this@MyWritePage, item)
                || super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return MainActivityUtil.onCreateOptionsMenu(this@MyWritePage, menu)
    }



    private fun getBoardList(boardService: BoardController, sessionNicname: String) {
        val boardListCall: Call<List<BoardDto>> = boardService.getMyBoard(sessionNicname)

        boardListCall.enqueue(object : Callback<List<BoardDto>> {
            override fun onResponse(call: Call<List<BoardDto>>, response: Response<List<BoardDto>>) {
                if (response.isSuccessful) {
                    val boardListResponse = response.body()
                    Log.d("lsy", "test: " + boardListResponse)
                    boardListResponse?.let { boardList ->
                        // 세션 닉네임과 동일한 닉네임으로 작성된 게시물 필터링
                        val filteredList = boardList.filter { it.userNicname == sessionNicname }
                        boardAdapter.setData(filteredList)
                    }
                } else {
                    Log.e("MyBoardList", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BoardDto>>, t: Throwable) {
                if (t is IOException) {
                    Log.e("MyBoardList", "Network Error: ${t.message}")
                } else if (t is HttpException) {
                    Log.e("MyBoardList", "HTTP Error: ${t.code()}")
                } else {
                    Log.e("MyBoardList", "Error: ${t.message}")
                }
            }
        })
    }
}