package com.example.foodmate.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.foodmate.databinding.ItemMainBinding
import com.example.foodmate.model.BarDto

class MyViewHolder(val binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root)

class BarAdapter(private val context: Context, private val barList: MutableList<BarDto>) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun getItemCount(): Int {
        return barList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val binding = holder.binding
        val bar = barList?.get(position)

        binding.BarTitle.text = bar?.main_TITLE
        val urlImg = bar?.main_IMG_NORMAL
        binding.BarAddr.text = bar?.addr1

        Glide.with(context)
            .asBitmap()
            .load(urlImg)
            .into(object : CustomTarget<Bitmap>(200, 200) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    binding.BarImg.setImageBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 이미지 로딩이 취소되었을 때의 동작을 정의하려면 여기에 코드를 추가하세요.
                }
            })
    }

    fun setData(data: List<BarDto>?) {
        barList.clear()
        if (data != null) {
            barList.addAll(data)
        }
        notifyDataSetChanged()
    }
}