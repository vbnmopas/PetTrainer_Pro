package org.techtown.multiwindow

data class ChatGPTResponse(
    val choices: List<Choice>
) {
    data class Choice(
        val message: ChatMsg
    )
}
