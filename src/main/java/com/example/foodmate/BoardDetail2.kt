package com.example.foodmate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.foodmate.Util.MainActivityUtil
import com.example.foodmate.controller.BoardController
import com.example.foodmate.controller.MeetingController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityBoardDetail2Binding
import com.example.foodmate.model.BoardDto
import com.example.foodmate.model.MeetingDto
import com.example.foodmate.model.MemberDto
import com.example.foodmate.model.MessageDto
import com.example.foodmate.network.RetrofitBuilder
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.auth.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BoardDetail2 : AppCompatActivity() {
    private val TAG: String = "BoardDetail2"
    private lateinit var binding: ActivityBoardDetail2Binding
    private lateinit var boardService: BoardController
    private lateinit var meetingService: MeetingController
    private lateinit var menu: Menu
    private lateinit var boardId:String
    private lateinit var nickname:String
    private lateinit var member:MemberDto
    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDetail2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        boardService = RetrofitBuilder.BoardService()
        meetingService = RetrofitBuilder.MeetingService()
        boardId = intent.getStringExtra("boardId").toString()
        nickname = SharedPreferencesUtil.getSessionNickname(this@BoardDetail2) ?: ""
        member = MemberDto(nickname = nickname, pw = "", id = "")
        Log.d("lsy","nickname : ${nickname}")



        val regList: Button = findViewById(R.id.reg_list)
        val heart : Button = findViewById(R.id.button_favorite)
        val chattingroom : Button = findViewById(R.id.chattingroom)

        context = this@BoardDetail2

        regList.setOnClickListener {
            // 목록 버튼으로 클릭 시 MainActivity로 이동
            val intent = Intent(this@BoardDetail2, MainActivity::class.java)
            startActivity(intent)
        }

//        heart.setOnClickListener {
//            // 예: 수정 버튼을 클릭 시 BoardUpdate로 이동
//            val intent = Intent(this@BoardDetail2, BoardUpdate::class.java)
//            startActivity(intent)
//        }

        chattingroom.setOnClickListener {
            // 예: 삭제 버튼을 클릭 시 BoardDelete로 이동
            val boardId = intent.getStringExtra("boardId")

            if (boardId != null) {
                val intent = Intent(this@BoardDetail2, MeetingActivity::class.java)
                intent.putExtra("boardId", boardId)
                startActivity(intent)
                addUser()
            } else {
                Log.e("BoardDetail2", "Error: Board ID is null")
            }

//            addUser()
        }
        val boardId = intent.getStringExtra("boardId")

        if (boardId != null) {
            getBoardDetail(boardId)
        } else {
            Log.e("BoardDetail2", "Error: Board ID is null")
        }

        //메인 유틸 코드
        MainActivityUtil.initViews(this@BoardDetail2)
        val plusButton = findViewById<ImageButton>(R.id.plus)
        plusButton.setOnClickListener {
            MainActivityUtil.showPopupMenu(this, plusButton)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fragmentManager = supportFragmentManager
        val mainLayout = findViewById<View>(R.id.mainLayout)
        MainActivityUtil.setBottomNavigationListener(bottomNavigationView, fragmentManager,mainLayout)
    }

    private fun addUser() {
        val addMemberCall: Call<ResponseBody> = meetingService.addMember(boardId, member)

        addMemberCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 회원 추가 성공 처리 로직을 작성하세요.
                } else {
                    Log.e("BoardDetail2", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("BoardDetail2", "Error: ${t.message}")
            }
        })
    }

    //메인 유틸 함수 호출
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainActivityUtil.onOptionsItemSelected(this, item)
                || super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return MainActivityUtil.onCreateOptionsMenu(this@BoardDetail2, menu)
    }

    private fun getBoardDetail(boardId: String) {
        val boardDetailCall: Call<BoardDto> = boardService.getBoardDetail(boardId)

        boardDetailCall.enqueue(object : Callback<BoardDto> {
            override fun onResponse(call: Call<BoardDto>, response: Response<BoardDto>) {
                if (response.isSuccessful) {
                    val boardDetailResponse = response.body()
                    boardDetailResponse?.let {
                        // 상세 정보를 처리하는 로직을 작성하세요.
                        // 예: 받은 데이터를 사용하여 UI에 표시
                        binding.UserNickname.text = it.userNicname
                        binding.boardcontent.text = it.content
                        binding.BarName.text = it.barName
                        binding.UserCount.text = it.memberCount
                        binding.MeetDate.text = it.meetdate.toString()
                        binding.RegDate.text = it.regdate.toString()

                        val urlImg = it.barImg

                        Glide.with(context)
                            .asBitmap()
                            .load(urlImg)
                            .into(object : CustomTarget<Bitmap>(200, 200) {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    binding.BoardImg.setImageBitmap(resource)
                                }
                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // 이미지 로딩이 취소되었을 때의 동작을 정의하려면 여기에 코드를 추가하세요.
                                }
                            })
                    }
                } else {
                    Log.e("BoardDetail", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<BoardDto>, t: Throwable) {
                Log.e("BoardDetail", "Error: ${t.message}")
            }
        })
    }
}