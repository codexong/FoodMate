package com.example.foodmate.model

import java.io.Serializable

data class MeetingDto(
    val boardid: String,
    val meeting_title: String,
    val meeting_content: String,
    var user: List<MemberDto>,
    val date: String,
    val messages: List<MessageDto>
) : Serializable