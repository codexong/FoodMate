package com.example.foodmate.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.adapter.BoardAdapter
import com.example.foodmate.controller.BoardController
import com.example.foodmate.databinding.FragmentHomeBinding
import com.example.foodmate.model.BoardDto
import com.example.foodmate.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var boardService: BoardController
    private lateinit var mainLayout: RecyclerView
    private lateinit var boardAdapter: BoardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        boardService = RetrofitBuilder.BoardService()

        mainLayout = binding.mainLayout
        mainLayout.layoutManager = LinearLayoutManager(requireContext())

        val boardList: MutableList<BoardDto> = mutableListOf()
        boardAdapter = BoardAdapter(requireContext(), boardList)
        mainLayout.adapter = boardAdapter
        getBoardList(boardService)
    }

    private fun getBoardList(boardService: BoardController) {
        val boardListCall: Call<List<BoardDto>> = boardService.getAllBoard()

        boardListCall.enqueue(object : Callback<List<BoardDto>> {
            override fun onResponse(call: Call<List<BoardDto>>, response: Response<List<BoardDto>>) {
                if (response.isSuccessful) {
                    val boardListResponse = response.body()
                    boardListResponse?.let {
                        boardAdapter.setData(it)
                    }
                } else {
                    Log.e("HomeFragment", "Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<List<BoardDto>>, t: Throwable) {
                if (t is IOException) {
                    Log.e("HomeFragment", "Network Error: ${t.message}")
                } else if (t is HttpException) {
                    Log.e("HomeFragment", "HTTP Error: ${t.code()}")
                } else {
                    Log.e("HomeFragment", "Error: ${t.message}")
                }
            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // 자원 해제 코드 작성
        // 예시: boardAdapter의 데이터를 초기화하여 자원을 해제
        boardAdapter.setData(emptyList())
    }
}