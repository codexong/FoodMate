package com.example.foodmate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import com.example.foodmate.Util.MainActivityUtil
import com.example.foodmate.controller.BarController
import com.example.foodmate.controller.BoardController
import com.example.foodmate.controller.MeetingController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.model.BarDto
import com.example.foodmate.model.BoardDto
import com.example.foodmate.model.MeetingDto
import com.example.foodmate.model.MemberDto
import com.example.foodmate.model.MessageDto
import com.example.foodmate.network.RetrofitBuilder
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BoardInsert : AppCompatActivity() {

    private val TAG: String = "BoardInsert"

    private lateinit var txtAppointment: TextView
    private lateinit var btnCalendar: Button

    private lateinit var calendar: Calendar
    private lateinit var dateFormat: DateFormat
    private lateinit var timeFormat: DateFormat

    private lateinit var menu: Menu

    //식당 리스트
    private lateinit var dropBarList: Spinner
    private lateinit var barService: BarController
    private lateinit var barListResponse: List<BarDto>

    private lateinit var boardService: BoardController

    private lateinit var meetingService: MeetingController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_insert)


        val regButton: Button = findViewById(R.id.reg_button)
        val regCancel: Button = findViewById(R.id.reg_cancel)

        //식당 리스트 컨트롤러
        dropBarList = findViewById(R.id.drop_barlist)
        barService = RetrofitBuilder.BarService()

        //보드 레트로핏 연결
        boardService = RetrofitBuilder.BoardService()

        meetingService = RetrofitBuilder.MeetingService()


        getRestaurantList(barService)

        regButton.setOnClickListener {
            sendBoardData()
        }

        regCancel.setOnClickListener {
            // 작성 취소 버튼 클릭 시 처리할 로직 작성
            // 예: 메인화면 으로 이동
            val intent = Intent(this@BoardInsert, MainActivity::class.java)
            startActivity(intent)
        }

        txtAppointment = findViewById(R.id.appointment)
        btnCalendar = findViewById(R.id.btn_calendar)

        calendar = Calendar.getInstance()
        dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext)
        timeFormat = android.text.format.DateFormat.getTimeFormat(applicationContext)

        btnCalendar.setOnClickListener {
            showDatePicker()
        }
        //메인 유틸 코드
        MainActivityUtil.initViews(this@BoardInsert)
        val plusButton = findViewById<ImageButton>(R.id.plus)
        plusButton.setOnClickListener {
            MainActivityUtil.showPopupMenu(this@BoardInsert, plusButton)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fragmentManager = supportFragmentManager
        val mainLayout = findViewById<View>(R.id.mainLayout)
        MainActivityUtil.setBottomNavigationListener(bottomNavigationView, fragmentManager,mainLayout)
    }

    //메인 유틸 함수 호출
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainActivityUtil.onOptionsItemSelected(this@BoardInsert, item)
                || super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return MainActivityUtil.onCreateOptionsMenu(this@BoardInsert, menu)
    }

    //식당이름 리스트 불러오기
    private fun getRestaurantList(barService: BarController) {
        val restaurantListCall: Call<List<BarDto>> = barService.getAllBars()

        restaurantListCall.enqueue(object : Callback<List<BarDto>> {
            override fun onResponse(call: Call<List<BarDto>>, response: Response<List<BarDto>>) {
                if (response.isSuccessful) {
                    barListResponse = response.body() ?: emptyList() // 식당 목록을 barListResponse에 할당
                    // 식당 이름만 추출하여 어댑터에 설정
                    val restaurantNames = barListResponse.map { bar -> bar.main_TITLE }
                    val adapter = ArrayAdapter(
                        this@BoardInsert,
                        android.R.layout.simple_spinner_item,
                        restaurantNames
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    dropBarList.adapter = adapter
                } else {
                    Log.e("RestaurantList", "Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BarDto>>, t: Throwable) {
                if (t is IOException) {
                    Log.e("BarList", "Network Error: ${t.message}")
                } else if (t is HttpException) {
                    Log.e("BarList", "HTTP Error: ${t.code()}")
                } else {
                    Log.e("BarList", "Error: ${t.message}")
                }
            }
        })
    }
    private fun getSelectedBarImageUrl(barName: String): String {
        val selectedBar = barListResponse.firstOrNull { it.main_TITLE == barName }
        return selectedBar?.main_IMG_NORMAL ?: ""
    }

    // 날짜 선택
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showTimePicker()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
    // 시간 선택
    private fun showTimePicker() {
        val timePickerDialog = TimePickerDialog(
            this,
            TimePickerDialog.OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                // 선택한 날짜와 시간을 @+id/appointment 텍스트뷰에 표시
                val appointmentDateTime = dateFormat.format(calendar.time)
                    .toString() + " " + timeFormat.format(calendar.time).toString()
                txtAppointment.text = appointmentDateTime

                // 선택한 날짜와 시간을 다른 방식으로 활용하려면 이곳에 원하는 동작을 추가
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            android.text.format.DateFormat.is24HourFormat(applicationContext)
        )

        timePickerDialog.show()
    }
    //게시판에 입력된 데이터 -> 파이어베이스 -> insert
    private fun sendBoardData() {
        val userNickname = SharedPreferencesUtil.getSessionNickname(this@BoardInsert) ?: "" // 작성자 정보
        val title = findViewById<EditText>(R.id.boardtitle).text.toString()
        val content = findViewById<EditText>(R.id.boardcontent).text.toString()
        val barName = dropBarList.selectedItem.toString() // 선택된 식당 이름
        val barImg = getSelectedBarImageUrl(barName) // 해당 식당 이미지 URL 가져오기
        val memberCount = findViewById<EditText>(R.id.partyone).text.toString()
        val meetdate = dateFormat.format(calendar.time).toString() + " " + timeFormat.format(calendar.time).toString() // 만남 날짜와 시간
        val regdate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) // 등록 날짜

        boardService = RetrofitBuilder.BoardService()

        val board = BoardDto("", userNickname, title, content, barName, barImg, memberCount, meetdate, regdate)

        val call = boardService.insertBoard(board)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Log.d("lsy","response body 확인 : ${response.body().toString()}")

                    val boardid = response.body()?.string() // 보드 아이디 받아오기
                    boardid?.let {
                        // 보드 아이디가 성공적으로 받아와졌을 때 sendMeetingData 함수 호출
                        Log.d("lsy","boardid 확인 : ${boardid}")
                        Log.d("lsy","boardid - it 확인2  : ${it}")

                        sendMeetingData(it) // 수정된 부분: 받아온 보드 아이디 전달
                    }
                    Toast.makeText(applicationContext, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")
                } else {
                    // 전송 실패한 경우의 처리
                    Toast.makeText(applicationContext, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e(TAG, "통신 실패: ${t.message}")
                Toast.makeText(applicationContext, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // 미팅 부분에 -> insert
    private fun sendMeetingData(boardid: String) {
        val meeting_title = findViewById<EditText>(R.id.boardtitle).text.toString()
        val meeting_content = findViewById<EditText>(R.id.boardcontent).text.toString()
        val user = SharedPreferencesUtil.getSessionNickname(this@BoardInsert) ?: "" // 작성자 정보
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()) // 등록 날짜
        val messages: List<MessageDto> = emptyList() // 빈 리스트로 초기화하거나 실제 메시지 데이터를 포함

        meetingService = RetrofitBuilder.MeetingService()

        val userList = mutableListOf<MemberDto>()
        val member = MemberDto(nickname = user, pw = "", id = "") // 적절한 값으로 설정
        userList.add(member)
        val intent = Intent(this@BoardInsert, MainActivity::class.java)
        startActivity(intent)
        finish()
        val meeting = MeetingDto(boardid, meeting_title, meeting_content, userList, date, messages)

        val call = meetingService.insertMeeting(boardid, meeting)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")
                } else {
                    // 전송 실패한 경우의 처리
                    Toast.makeText(applicationContext, "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "응답 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 통신 실패 처리
                Log.e(TAG, "통신 실패: ${t.message}")
                Toast.makeText(applicationContext, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}