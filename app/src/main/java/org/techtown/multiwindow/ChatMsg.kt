package org.techtown.multiwindow

class ChatMsg(val role: String, val content: String) {

    override fun toString(): String {
        return "ChatMsg(role=$role, content=$content)"
    }

    companion object {
        // 기존 역할 상수
        const val ROLE_USER = "user" // 내 메시지
        const val ROLE_ASSISTANT = "assistant" // 챗봇 메시지

        // 메시지 타입 상수
        const val TYPE_MY_CHAT = 0 // 내 메시지 타입
        const val TYPE_BOT_CHAT = 1 // 챗봇 메시지 타입
    }
}
