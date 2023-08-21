package com.example.foodmate

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.foodmate.controller.MemberController
import com.example.foodmate.controller.PasswordHashUtil
import com.example.foodmate.databinding.ActivityJoinBinding
import com.example.foodmate.network.ImageUtil
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class JoinActivity : AppCompatActivity() {

    private val TAG: String = "JoinActivity"
    private lateinit var binding: ActivityJoinBinding
    private lateinit var apiService: MemberController
    private var selectedImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = RetrofitBuilder.MemberService()

        binding.img1.setOnClickListener { selectImageView(binding.img1) }
        binding.img2.setOnClickListener { selectImageView(binding.img2) }
        binding.img3.setOnClickListener { selectImageView(binding.img3) }
        binding.img4.setOnClickListener { selectImageView(binding.img4) }

        binding.btnRegister.setOnClickListener {
            val id = binding.editId.text.toString()
            val pw = binding.editPw.text.toString()
            val pwRe = binding.editPwRe.text.toString()
            val nickname = binding.editNickname.text.toString()

            // 사용자가 필수 입력 사항을 모두 입력하지 않은 경우
            if (id.isEmpty() || pw.isEmpty() || nickname.isEmpty()) {
                showDialog("blank")
                return@setOnClickListener
            }

            // 비밀번호가 일치하지 않는 경우
            if (pw != pwRe) {
                showDialog("not same")
                return@setOnClickListener
            }

            // 회원가입 API 호출
            val selectedImageResource = selectedImageView?.id?.let { getSelectedImageResource(it) }
            if (selectedImageResource != null) {
                val imageFile = getFileFromResource(selectedImageResource)
                val encodedImage = ImageUtil.encodeImageToBase64(imageFile)
                insertMember(id, pw, nickname, encodedImage)
            } else {
                // 이미지가 선택되지 않은 경우
                Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun insertMember(id: String, pw: String, nickname: String, encodedImage: String) {
        val hashedPw = PasswordHashUtil.hashPassword(pw) // 비밀번호를 해시하여 암호화

        val call: Call<ResponseBody> = apiService.insertMember(id, hashedPw, nickname, encodedImage)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val message = response.code()
                    if (message == 200) {
                        // 회원가입 성공 처리
                        showDialog("성공")
                    } else {
                        // 회원가입 실패 처리
                        showDialog("회원가입 실패")
                    }
                } else {
                    // 서버 오류 등의 상태코드가 반환된 경우
                    showDialog("서버오류로 회원가입 실패")
                }

                Log.d(TAG, "통신 성공 - HTTP 상태 코드: ${response.code()}")
                Log.d(TAG, "통신 성공 - 응답 메시지: ${response.body()?.toString()}")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                showDialog("통신 실패")
                Log.e(TAG, "통신 실패: ${t.message}")
            }
        })

        Log.d(TAG, "이미지: $encodedImage")
        Log.d(TAG, "아이디: $id")
        Log.d(TAG, "비밀번호: $pw")
        Log.d(TAG, "닉네임: $nickname")
    }


    private fun showDialog(message: String) {
        val dialogBuilder = AlertDialog.Builder(this@JoinActivity)

        dialogBuilder.setTitle("회원가입 결과")
        dialogBuilder.setMessage(message)

        val dialogListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Log.d(TAG, "확인 버튼 클릭")
                    if (message == "회원가입 성공") {

                    }
                }
            }
        }

        dialogBuilder.setPositiveButton("확인", dialogListener)
        dialogBuilder.show()
    }

    private fun selectImageView(imageView: ImageView) {
        selectedImageView?.isSelected = false
        selectedImageView?.scaleX = 1.0f
        selectedImageView?.scaleY = 1.0f

        imageView.isSelected = true
        imageView.scaleX = 1.2f
        imageView.scaleY = 1.2f

        selectedImageView = imageView
    }


    private fun getSelectedImageResource(selectedImageId: Int): Int? {
        return when (selectedImageId) {
            R.id.img1 -> R.drawable.ch1
            R.id.img2 -> R.drawable.ch2
            R.id.img3 -> R.drawable.ch3
            R.id.img4 -> R.drawable.ch4
            else -> null
        }
    }

    private fun getFileFromResource(resourceId: Int): File {
        val res = resources.getResourceName(resourceId)
        val resName = res.substringAfterLast('/')
        val fileName = "${externalCacheDir?.absolutePath}/$resName"
        val file = File(fileName)
        val inputStream = resources.openRawResource(resourceId)
        val outputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.flush()
        outputStream.close()
        return file
    }

}