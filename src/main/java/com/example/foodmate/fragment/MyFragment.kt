package com.example.foodmate.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.foodmate.R
import com.example.foodmate.UpdateActivity
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.FragmentMyBinding

class MyFragment : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private lateinit var nicknameTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        val view = binding.root
        nicknameTextView = binding.nickname

        // 세션에서 이미지 가져오기
        val sessionImage = SharedPreferencesUtil.getImage(requireContext())
        if (sessionImage != null) {
            binding.profileimg.setImageBitmap(sessionImage) // 세션에 저장된 이미지 설정
        }

        val sessionNickname = SharedPreferencesUtil.getSessionNickname(requireContext())
        Log.d("MyFragment", "$sessionNickname")
        setSessionNickname(sessionNickname)

        binding.profile.setOnClickListener {
            val intent = Intent(requireContext(), UpdateActivity::class.java)
            startActivity(intent)
        }


        return view
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setSessionNickname(nickname: String?) {
        nicknameTextView.text = nickname ?: "기본 닉네임"
    }
}