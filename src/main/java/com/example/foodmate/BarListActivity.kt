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
import com.example.foodmate.adapter.BarAdapter
import com.example.foodmate.controller.BarController
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.databinding.ActivityBarListBinding
import com.example.foodmate.model.BarDto
import com.example.foodmate.network.RetrofitBuilder
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class BarListActivity : AppCompatActivity() {
    private val TAG: String = "BarListActivity"

    private lateinit var binding: ActivityBarListBinding
    private lateinit var barService: BarController

    private lateinit var recyclerView: RecyclerView
    private lateinit var barAdapter: BarAdapter

    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MainActivityUtil.initViews(this@BarListActivity)
        val plusButton = findViewById<ImageButton>(R.id.plus)
        plusButton.setOnClickListener {
            MainActivityUtil.showPopupMenu(this, plusButton)
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val fragmentManager = supportFragmentManager
        val mainLayout = findViewById<View>(R.id.mainLayout)
        MainActivityUtil.setBottomNavigationListener(bottomNavigationView, fragmentManager,mainLayout)



        barService = RetrofitBuilder.BarService()

        recyclerView = findViewById(R.id.mainLayout)
        recyclerView.layoutManager = LinearLayoutManager(this@BarListActivity)

        val barList: MutableList<BarDto> = mutableListOf()
        barAdapter = BarAdapter(this@BarListActivity, barList)
        recyclerView.adapter = barAdapter

        getFoodList(barService)

        val isLoggedIn = SharedPreferencesUtil.checkLoggedIn(this)
        Log.d(TAG, "세션 유지 상태: $isLoggedIn")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return MainActivityUtil.onOptionsItemSelected(this, item)
                || super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        return MainActivityUtil.onCreateOptionsMenu(this@BarListActivity, menu)
    }

    private fun getFoodList(barService: BarController) {
        val foodListCall: Call<List<BarDto>> = barService.getAllBars()

        foodListCall.enqueue(object : Callback<List<BarDto>> {
            override fun onResponse(call: Call<List<BarDto>>, response: Response<List<BarDto>>) {
                if (response.isSuccessful) {
                    val barListResponse = response.body()
                    Log.d("lsy","test: " + barListResponse)
                    barListResponse?.let {
                        barAdapter.setData(it)
                    }
                } else {
                    Log.e("BarList", "Error: ${response.code()}")
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
}