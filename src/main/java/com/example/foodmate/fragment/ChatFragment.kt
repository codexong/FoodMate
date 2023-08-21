package com.example.foodmate.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.adapter.ChatListAdapter
import com.example.foodmate.controller.MeetingController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.FragmentChatBinding
import com.example.foodmate.model.MeetingDto
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var meetingService: MeetingController
    private lateinit var chatLayout: RecyclerView
    private lateinit var chatListAdapter: ChatListAdapter
    private lateinit var nickname: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        meetingService = RetrofitBuilder.MeetingService()

        chatLayout = binding.chatLayout
        chatLayout.layoutManager = LinearLayoutManager(requireContext())

        val meetingList: MutableList<MeetingDto> = mutableListOf()
        chatListAdapter = ChatListAdapter(requireContext(), meetingList)
        chatLayout.adapter = chatListAdapter

        nickname = SharedPreferencesUtil.getSessionNickname(requireContext()) ?: ""
        getMeetingList(meetingService, nickname)

    }

    private fun getMeetingList(meetingService: MeetingController, nickname: String) {
        val meetingListCall: Call<List<MeetingDto>> = meetingService.getMeetingByNickname(nickname)

        meetingListCall.enqueue(object : Callback<List<MeetingDto>> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<List<MeetingDto>>, response: Response<List<MeetingDto>>) {
                if (response.isSuccessful) {
                    val meetingListResponse = response.body()
                        meetingListResponse?.let {
                            chatListAdapter.setData(meetingListResponse)
                        }
                } else {
                    Log.e("HomeFragment", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<MeetingDto>>, t: Throwable) {
                Log.e("ChatFragment", "Error: ${t.message}")
            }

        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // 자원 해제 코드 작성
        // 예시: boardAdapter의 데이터를 초기화하여 자원을 해제
        chatListAdapter.setData(emptyList())
    }
}



