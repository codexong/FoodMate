package com.example.foodmate

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.adapter.ChatAdapter
import com.example.foodmate.controller.MeetingController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityMeetingBinding
import com.example.foodmate.model.MeetingDto
import com.example.foodmate.model.MessageDto
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeetingActivity : AppCompatActivity() {

//    private lateinit var meetingDto: MeetingDto
    val TAG: String = "MeetingActivity"
    private lateinit var binding: ActivityMeetingBinding
    private lateinit var meetingService: MeetingController

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var messageList: MutableList<MessageDto>
    private lateinit var boardId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // meetingid = boardId 
        boardId = intent.getStringExtra("boardId").toString()
        Log.d("lsy","boardId : ${boardId}")

        chatAdapter = ChatAdapter(this, mutableListOf())

        meetingService = RetrofitBuilder.MeetingService()
        binding.chatListRecyclerView.adapter = chatAdapter

        messageRecyclerView = findViewById(R.id.chatListRecyclerView)
        messageList = mutableListOf()

        chatAdapter = ChatAdapter(this, messageList)
        messageRecyclerView.adapter = chatAdapter

        messageRecyclerView.layoutManager = LinearLayoutManager(this)

        // 채팅방 관련 메세지 전부다 가지고오기. 
        loadMessages(boardId)

        val meetingContent = boardId
        val drawerTitle: TextView = findViewById(R.id.drawerTitle)
        drawerTitle.text = meetingContent
//        Log.d(TAG, "응답 코드: ${meetingDto.meeting_title}")

//        binding.updateButton.setOnClickListener {
//            val updatedMeeting = // 업데이트할 MeetingDto 객체 생성
//            val meetingId = meetingDto.meeting_id
//            updateMeeting(meetingId, updatedMeeting)
//        }
//
//        binding.deleteButton.setOnClickListener {
//            val meetingId = meetingDto.meeting_id
//            deleteMeeting(meetingId)
//        }

//        binding.chatListRecyclerView.setOnClickListener {
//            val meetingId = meetingDto.boardid
//            loadMessages(meetingId)
//        }

        binding.chatSendBtn.setOnClickListener {
            val meetingId = boardId
            val messageContent = binding.chatMsgEdit.text.toString()
            sendMessage(meetingId, messageContent)
        }
    }

//    private fun updateMeeting(meetingId: String, updatedMeeting: MeetingDto) {
//        val call = meetingService.updateMeeting(meetingId, updatedMeeting)
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    // 업데이트 성공
//                    // TODO: 업데이트 완료 후 동작 처리
//                } else {
//                    // 업데이트 실패
//                    Toast.makeText(this@MeetingActivity, "Failed to update meeting", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                // 통신 실패
//                Toast.makeText(this@MeetingActivity, "Failed to update meeting", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

//    private fun deleteMeeting(meetingId: String) {
//        val call = meetingService.deleteMeeting(meetingId)
//        call.enqueue(object : Callback<MeetingDto> {
//            override fun onResponse(call: Call<MeetingDto>, response: Response<MeetingDto>) {
//                if (response.isSuccessful) {
//                    // 삭제 성공
//                    // TODO: 삭제 완료 후 동작 처리
//                } else {
//                    // 삭제 실패
//                    Toast.makeText(this@MeetingActivity, "Failed to delete meeting", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<MeetingDto>, t: Throwable) {
//                // 통신 실패
//                Toast.makeText(this@MeetingActivity, "Failed to delete meeting", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    private fun loadMessages(meetingId: String) {
        val call = meetingService.getOneMeeting(meetingId)
        call.enqueue(object : Callback<MeetingDto> {
            override fun onResponse(call: Call<MeetingDto>, response: Response<MeetingDto>) {
                if (response.isSuccessful) {
                    val meeting = response.body()
                    if (meeting != null) {
                        val messages = meeting.messages
                        chatAdapter.submitList(messages)
                        chatAdapter.notifyDataSetChanged()
                    }
                } else {
                    // 메시지 불러오기 실패
                    Toast.makeText(this@MeetingActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MeetingDto>, t: Throwable) {
                // 통신 실패
                Toast.makeText(this@MeetingActivity, "Failed to load messages", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendMessage(meetingId: String, messageContent: String) {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.KOREA)//한국 날짜 시간 변경
        val curTime = dateFormat.format(Date(time))
        val nickname = SharedPreferencesUtil.getSessionNickname(this@MeetingActivity,) ?: ""
        val message = MessageDto(nickname, messageContent, curTime)
        Log.d("lsy","message의 curTime 내용 : " + curTime)
        val call = meetingService.addMessage(meetingId, message)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 메시지 보내기 성공
                    val meetingId = boardId
                    loadMessages(meetingId)
                    // TODO: 메시지 보내기 완료 후 동작 처리
                } else {
                    // 메시지 보내기 실패
                    Toast.makeText(
                        this@MeetingActivity,
                        "Failed to send message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패
                Toast.makeText(this@MeetingActivity, "Failed to send message", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }


}