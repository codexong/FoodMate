package com.example.foodmate

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import com.example.foodmate.network.ImageUtil.encodeBitmapToBase64
import com.example.foodmate.controller.MemberController
import com.example.foodmate.controller.PasswordHashUtil
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityUpdateBinding
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateActivity : AppCompatActivity() {

    private val TAG: String = "UpdateActivity"
    private lateinit var binding: ActivityUpdateBinding
    private lateinit var apiService: MemberController
    private var selectedBitmap: Bitmap? = null // 추가된 부분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionId = SharedPreferencesUtil.getSessionId(this)
        val sessionPw = SharedPreferencesUtil.getSessionPw(this)
        val sessionNickname = SharedPreferencesUtil.getSessionNickname(this)

        binding.editId.setText(sessionId)
        binding.editPw.setText(sessionPw)
        binding.editNickname.setText(sessionNickname)

        apiService = RetrofitBuilder.MemberService()

        binding.img1.setOnClickListener { selectImageView(binding.img1) }
        binding.img2.setOnClickListener { selectImageView(binding.img2) }
        binding.img3.setOnClickListener { selectImageView(binding.img3) }
        binding.img4.setOnClickListener { selectImageView(binding.img4) }

        // 회원 정보 수정 버튼
        binding.btnUpdate.setOnClickListener {
            val id = binding.editId.text.toString()
            val password = binding.editPw.text.toString()
            val nickname = binding.editNickname.text.toString()

            // 회원 정보 업데이트
            updateMemberInfo(id, password, nickname, selectedBitmap)

            selectedBitmap?.let { it1 ->
                SharedPreferencesUtil.reloadSession(this, id, password, nickname,
                    it1
                )
            }
            Log.d(TAG, "리로드된 세션값: $id, $password, $nickname")

            selectedBitmap?.let { it1 ->
                SharedPreferencesUtil.updateSession(this, id, password, nickname,
                    it1
                )
            }
            Log.d(TAG, "저장된 세션값: $id, $password, $nickname")

            val intent = Intent(this@UpdateActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 회원 탈퇴 버튼
        binding.btnDelete.setOnClickListener {
            val id = binding.editId.text.toString()
            val password = binding.editPw.text.toString()
            val nickname = binding.editNickname.text.toString()

            SharedPreferencesUtil.clearSession(this)
            Log.d(TAG, "삭제된 세션값: $id, $password, $nickname")

            deleteMember(id, password, nickname)

            val intent = Intent(this@UpdateActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateMemberInfo(id: String, pw: String, nickname: String, bitmap: Bitmap?) { // 수정된 부분
        val hashedPw = PasswordHashUtil.hashPassword(pw)
        val encodedImage = bitmap?.let { encodeBitmapToBase64(it) } ?: ""

        // 회원 정보 업데이트
        val call: Call<ResponseBody> = apiService.updateMember(id, hashedPw, nickname, encodedImage)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val updateResponse = response.code()
                    if (updateResponse == 200) {
                        Log.d(TAG, "회원 정보 수정 완료")
                        showDialog("회원수정 성공")
                    } else {
                        val message = updateResponse ?: "회원 정보 수정 실패"
                        Log.d(TAG, "$updateResponse")
                        showDialog("실패")
                        // 실패 처리
                    }

                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = "회원 정보 수정 실패 - ${response.code()}: $errorBody"
                    Log.d(TAG, message)
                    // 실패 처리
                }

                Log.d(TAG, "통신 성공 - HTTP 상태 코드: ${response.code()}")
                Log.d(TAG, "통신 성공 - 응답 메시지: ${response.body()?.toString()}")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showDialog("통신 실패")
                Log.e(TAG, "통신 실패: ${t.message}")
                // 실패 처리
            }
        })
    }

    private fun showDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(this@UpdateActivity)

        dialogBuilder.setTitle("회원수정 결과")
        dialogBuilder.setMessage(message)

        val dialogListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Log.d(TAG, "확인 버튼 클릭")
                    if (message == "회원수정 성공") {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        dialogBuilder.setPositiveButton("확인", dialogListener)
        dialogBuilder.show()
    }

    private fun deleteMember(id: String, password: String, nickname: String) {
        val deleteMemberController = DeleteMemberController()

        deleteMemberController.deleteMember(this, id, password, nickname) { isSuccess ->
            if (isSuccess) {
                runOnUiThread {
                    Log.d(TAG, "회원 탈퇴 성공")
                    val intent = Intent(this@UpdateActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                runOnUiThread {
                    Log.d(TAG, "회원 탈퇴 실패")
                }
            }
        }
    }

    private fun selectImageView(imageView: ImageView) {

        // Set the selected image view appearance
        imageView.isSelected = true
        imageView.scaleX = 1.2f
        imageView.scaleY = 1.2f

        // Deselect other image views
        val imageViews = listOf(binding.img1, binding.img2, binding.img3, binding.img4)
        imageViews.filterNot { it == imageView }
            .forEach {
                it.isSelected = false
                it.scaleX = 1.0f
                it.scaleY = 1.0f
            }

        // Get the selected bitmap
        selectedBitmap = imageView.drawable.toBitmap()
    }
}