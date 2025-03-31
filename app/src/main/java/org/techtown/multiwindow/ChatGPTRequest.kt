package org.techtown.multiwindow

data class ChatGPTRequest(
    val model: String,
    val messages: List<ChatMsg>
)
