package com.example.foodmate.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.foodmate.controller.SharedPreferencesUtil
import com.example.foodmate.controller.TodoController
import com.example.foodmate.databinding.ItemListBinding
import com.example.foodmate.model.TodoDto
import com.example.foodmate.network.RetrofitBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//private lateinit var todoService: TodoController
//boardService = RetrofitBuilder.BoardService()

class TodoViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

class TodoAdapter(private val context: Context, private val todoList: MutableList<TodoDto>) :
    RecyclerView.Adapter<TodoViewHolder>() {
    private val sessionNickname = SharedPreferencesUtil.getSessionNickname(context)

    override fun getItemCount(): Int {
        return todoList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val binding = holder.binding
        val todo = todoList[position]

        binding.title.text = todo.title
        binding.contents.text = todo.memo

        binding.root.tag = todo.todoid  // Set the todoId as the tag value

        binding.todoDelete.setOnClickListener {
            val todoId = binding.root.tag as? String

            if (todoId != null) {
                deleteTodo(todoId)
            } else {
                Log.e("TodoDelete", "Error: Todo ID is null")
            }
        }
    }

    private fun deleteTodo(todoId: String) {
        val todoService = RetrofitBuilder.TodoService()
        val deleteTodoCall: Call<ResponseBody> = todoService.deleteTodo(todoId)

        deleteTodoCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // Todo deleted successfully
                    // Handle the response, e.g., display a success message
                    Log.d("TodoDelete", "Todo deleted successfully")

                    // Refresh the adapter data after deletion
                    todoList.removeAll { it.todoid == todoId }
                    notifyDataSetChanged()
                } else {
                    Log.e("TodoDelete", "Error: ${response.code()}")
                    // Handle the error response, e.g., display an error message
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("TodoDelete", "Error: ${t.message}")
                // Handle the failure, e.g., display an error message
            }
        })
    }


    fun setData(data: List<TodoDto>?) {
        todoList.clear()
        if (data != null) {
            todoList.addAll(data)
        }
        notifyDataSetChanged()
    }
}