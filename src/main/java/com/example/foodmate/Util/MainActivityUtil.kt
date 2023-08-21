package com.example.foodmate.Util

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import com.example.foodmate.BarListActivity
import com.example.foodmate.BoardInsert
import com.example.foodmate.LoginActivity
import com.example.foodmate.MyWritePage
import com.example.foodmate.R
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.fragment.CalendarFragment
import com.example.foodmate.fragment.ChatFragment
import com.example.foodmate.fragment.HomeFragment
import com.example.foodmate.fragment.MyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

object MainActivityUtil {
    private const val TAG: String = "MainActivityUtil"

    fun initViews(activity: AppCompatActivity) {

        // 툴바 설정
        val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
        activity.setSupportActionBar(toolbar)

        // 플러스 버튼 설정
        val plusButton = activity.findViewById<ImageButton>(R.id.plus)
        plusButton.setOnClickListener { showPopupMenu(activity, plusButton) }

        // 사용자 로그인 상태 확인
        val isLoggedIn = SharedPreferencesUtil.checkLoggedIn(activity)
        Log.d(TAG, "세션 유지 상태: $isLoggedIn")

        if (!isLoggedIn) {
            // 사용자가 로그인되어 있지 않으면 LoginActivity로 이동
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        } else {
            // 회원 탈퇴 후 세션 리로드
            val sessionId = SharedPreferencesUtil.getSessionId(activity)
            val sessionPw = SharedPreferencesUtil.getSessionPw(activity)
            val sessionNickname = SharedPreferencesUtil.getSessionNickname(activity)

            if (sessionId != null && sessionPw != null && sessionNickname != null) {
                SharedPreferencesUtil.reloadSessionAfterWithdrawal(
                    activity,
                    sessionId,
                    sessionPw,
                    sessionNickname
                )
            }
        }
    }

    fun onCreateOptionsMenu(activity: AppCompatActivity, menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.menu_toolbar, menu)

        val isLoggedIn = SharedPreferencesUtil.checkLoggedIn(activity)
        val loginMenuItem = menu.findItem(R.id.login)
        val logoutMenuItem = menu.findItem(R.id.logout)

        if (isLoggedIn) {
            // 로그인 상태인 경우
            loginMenuItem.isVisible = false // 로그인 메뉴 숨기기
            logoutMenuItem.isVisible = true // 로그아웃 메뉴 보이기

            // 닉네임 가져오기
            val nickname = SharedPreferencesUtil.getSessionNickname(activity)

            // 닉네임을 툴바에 표시
            val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
            toolbar.subtitle = "$nickname"
        } else {
            // 로그아웃 상태인 경우
            loginMenuItem.isVisible = true // 로그인 메뉴 보이기
            logoutMenuItem.isVisible = false // 로그아웃 메뉴 숨기기

            // 로그인 상태가 아니므로 닉네임을 툴바에서 제거
            val toolbar = activity.findViewById<Toolbar>(R.id.toolbar)
            toolbar.subtitle = null
        }

        // 회원 탈퇴 후 세션 데이터가 없는 경우 로그인 메뉴만 표시되도록 처리
        val sessionExists = SharedPreferencesUtil.checkSessionExists(activity)
        if (!sessionExists) {
            logoutMenuItem.isVisible = false // 로그아웃 메뉴 숨기기
        }

        return true
    }

    fun onOptionsItemSelected(activity: AppCompatActivity, item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.login) {
            // 사용자가 이미 로그인 화면에 있으므로 다시 이동할 필요가 없습니다.
            return true
        } else if (itemId == R.id.logout) {
            // 로그아웃 처리
            SharedPreferencesUtil.setLoggedIn(activity, false) // 로그인 상태를 false로 설정합니다.
            activity.invalidateOptionsMenu() // 옵션 메뉴를 다시 그리도록 호출합니다.

            // MainActivity2(로그인 화면)로 이동합니다.
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
            return true
        }
        return false
    }

    fun showPopupMenu(activity: AppCompatActivity, view: View) {
        val popupMenu = PopupMenu(activity, view)
        popupMenu.inflate(R.menu.board_menu)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == R.id.writing) {
                // 모집글 작성 메뉴 클릭 시 처리할 로직 작성
                // 예: BoardInsertActivity로 이동
                val intent = Intent(activity, BoardInsert::class.java)
                activity.startActivity(intent)
                return@OnMenuItemClickListener true
            } else if (itemId == R.id.mywriteview) {
                // 내가 작성한 글 보기 메뉴 클릭 시 처리할 로직 작성
                // 예: MyWritePage로 이동
                val intent = Intent(activity, MyWritePage::class.java)
                activity.startActivity(intent)
                return@OnMenuItemClickListener true
            } else if (itemId == R.id.barlist) {
                // 내가 작성한 글 보기 메뉴 클릭 시 처리할 로직 작성
                // 예: MyWritePage로 이동
                val intent = Intent(activity, BarListActivity::class.java)
                activity.startActivity(intent)
                return@OnMenuItemClickListener true
            }
            false
        })
        popupMenu.show()
    }
    fun setBottomNavigationListener(
        bottomNavigationView: BottomNavigationView,
        fragmentManager: FragmentManager,
        mainLayout: View
    ) {
        bottomNavigationView.setOnNavigationItemSelectedListener(TabSelectedListener(fragmentManager, mainLayout))
    }

    private class TabSelectedListener(
        private val fragmentManager: FragmentManager,
        private val mainLayout: View?
    ) : BottomNavigationView.OnNavigationItemSelectedListener {
        @SuppressLint("NonConstantResourceId")
        override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
            val itemId = menuItem.itemId
            if (itemId == R.id.tab_home) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
                mainLayout?.visibility = View.GONE
                return true
            } else if (itemId == R.id.tab_chat) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ChatFragment())
                    .commit()
                mainLayout?.visibility = View.GONE
                return true
            } else if (itemId == R.id.tab_calendar) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, CalendarFragment())
                    .commit()
                mainLayout?.visibility = View.GONE
                return true
            } else if (itemId == R.id.tab_my) {
                fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, MyFragment())
                    .commit()
                mainLayout?.visibility = View.GONE
                return true
            }
            return false
        }
    }
}