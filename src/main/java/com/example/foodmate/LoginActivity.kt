package com.example.foodmate

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.example.foodmate.controller.MemberController
import com.example.foodmate.controller.PasswordHashUtil
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityLoginBinding
import com.example.foodmate.model.LoginResponse
import com.example.foodmate.model.MemberDto
import com.example.foodmate.network.ImageUtil
import com.example.foodmate.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private val TAG: String = "LoginActivity"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var apiService: MemberController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitBuilder.MemberService()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val loginButton = binding.btnLogin
        loginButton.setOnClickListener {
            val id = binding.editId.text.toString()
            val pw = binding.editPw.text.toString()
            val nickname = binding.editNickname.text.toString()

            if (id.isEmpty() || pw.isEmpty()) {
                showDialog("blank")
                return@setOnClickListener
            }

            login(id, pw, nickname)
        }

        val joinButton = binding.btnRegister
        joinButton.setOnClickListener {
            val intent = Intent(applicationContext, JoinActivity::class.java)
            startActivityForResult(intent, 101)
        }
    }

    private fun login(id: String, pw: String, nickname: String) {
        val hashedPw = PasswordHashUtil.hashPassword(pw) // 비밀번호를 해싱합니다.
        val member = MemberDto(id, hashedPw, nickname)
        val call = apiService.login(member)
        Log.d(TAG, "로그인 요청 - ID: $id, PW: $pw")
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null && loginResponse.status == "success") { // Updated condition
                        // 로그인 성공 처리
                        Log.d(TAG, "로그인 성공")
                        showDialog("success")

                        val sessionId = loginResponse.sessionId
                        val sessionPw = loginResponse.sessionPw
                        val sessionNickname = loginResponse.sessionNickname

                        // 디코딩된 이미지를 가져와 세션에 저장
                        val encodedImage = loginResponse.sessionImage
                        val decodedImage = ImageUtil.decodeBase64ToBitmap(encodedImage)
                        if (decodedImage != null) {
                            SharedPreferencesUtil.saveImage(this@LoginActivity, decodedImage)
                        }

                        // 추가: 닉네임 정보를 가져오는 API 호출 및 세션 저장
                        getNicknameAndSaveSession(sessionId, sessionPw, sessionNickname)
                    } else {
                        // 로그인 실패 처리
                        Log.d(TAG, "로그인 실패")
                        showDialog("fail")
                    }
                } else {
                    // API 요청 실패 처리
                    Log.d(TAG, "API 요청 실패")
                    showDialog("fail")
                }
                Log.d(TAG, "통신 성공 - HTTP 상태 코드: ${response.code()}")
                Log.d(TAG, "통신 성공 - 응답 메시지: ${response.body()?.toString()}")
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // 통신 실패 처리
                showDialog("fail")
                Log.e(TAG, "통신 실패: ${t.message}")
            }
        })
    }

    private fun getNicknameAndSaveSession(sessionId: String, sessionPw: String, sessionNickname: String) {
        apiService.getMemberDetail(sessionId).enqueue(object : Callback<MemberDto> { // Assuming getMemberDetail API returns Member
            override fun onResponse(call: Call<MemberDto>, response: Response<MemberDto>) {
                if (response.isSuccessful) {
                    // 닉네임 정보 가져오기 성공
                    val member = response.body()

                    if (member != null) {
                        val nickname = member.nickname

                        // 세션 저장
                        SharedPreferencesUtil.saveSession(this@LoginActivity, sessionId, sessionPw, nickname)

                        Log.d(TAG, "Saving session ID: $sessionId, session PW: $sessionPw, nickname: $nickname")

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showDialog("fail")
                    }
                } else {
                    // API 호출 실패 처리
                    showDialog("fail")
                }
            }

            override fun onFailure(call: Call<MemberDto>, t: Throwable) {
                // 통신 실패 처리
                showDialog("fail")
                Log.e(TAG, "통신 실패: ${t.message}")
            }
        })
    }

    // 로그인 성공/실패 시 다이얼로그를 띄워주는 메소드
    private fun showDialog(type: String) {
        val dialogBuilder = AlertDialog.Builder(this)

        if (type == "success") {
            dialogBuilder.setTitle("로그인 성공")
            dialogBuilder.setMessage("로그인 성공!")
        } else if (type == "fail") {
            dialogBuilder.setTitle("로그인 실패")
            dialogBuilder.setMessage("아이디와 비밀번호를 확인해주세요")
        } else if (type == "blank") {
            dialogBuilder.setTitle("입력 필요")
            dialogBuilder.setMessage("아이디와 비밀번호를 입력해주세요")
        }

        val dialogListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Log.d(TAG, "확인 버튼 클릭")
                    if (type == "success") {
                        // 로그인 성공 시 처리할 작업 수행
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        dialogBuilder.setPositiveButton("확인", dialogListener)
        val dialog = dialogBuilder.create() // 다이얼로그 객체 생성
        dialog.show() // 다이얼로그 표시
    }
}