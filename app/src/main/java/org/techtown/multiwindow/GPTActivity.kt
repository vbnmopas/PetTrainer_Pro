package org.techtown.multiwindow
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GPTActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatMsgAdapter
    private lateinit var btnSend: Button
    private lateinit var etMsg: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var chatMsgList: MutableList<ChatMsg>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpt)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        btnSend = findViewById(R.id.btn_send)
        etMsg = findViewById(R.id.et_msg)
        progressBar = findViewById(R.id.progressBar)

        // Initialize the list of chat messages
        chatMsgList = mutableListOf()

        // Set up the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ChatMsgAdapter()
        recyclerView.adapter = adapter

        // TextWatcher for EditText to enable/disable the send button
        etMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                btnSend.isEnabled = s?.length ?: 0 > 0
            }
        })

        // Send button click listener
        btnSend.setOnClickListener {
            val msg = etMsg.text.toString()
            val chatMsg = ChatMsg(ChatMsg.ROLE_USER, msg)
            adapter.addChatMsg(chatMsg)
            chatMsgList.add(chatMsg)  // Make sure to add it to the list as well
            etMsg.setText(null)

            // Hide the keyboard
            val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            // Show progress bar and disable touch
            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // Use lifecycleScope to launch a coroutine
            lifecycleScope.launch {
                sendMsgToChatGPT()
            }
        }
    }

    private suspend fun sendMsgToChatGPT() {
        val api = ApiClient.chatGPTApi

        val request = ChatGPTRequest(
            model = "gpt-3.5-turbo",
            messages = chatMsgList
        )

        // ✅ 사용자가 보낸 메시지 로그 출력
        Log.d("ChatGPT", "사용자 입력 메시지: ${chatMsgList.last().content}")

        try {
            // ✅ API 요청 시작 로그
            Log.d("ChatGPT", "ChatGPT API 요청 시작: $request")

            val response = api.getChatResponse(request)

            if (response.isSuccessful && response.body() != null) {
                val chatResponse = response.body()!!.choices[0].message.content

                // ✅ ChatGPT 응답 로그 출력
                Log.d("ChatGPT", "ChatGPT 응답: $chatResponse")

                adapter.addChatMsg(ChatMsg(ChatMsg.ROLE_ASSISTANT, chatResponse))

                // 로딩바 숨기기
                progressBar.visibility = View.GONE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            } else {
                // ✅ 응답 실패 로그
                Log.e("ChatGPT", "Error: ${response.message()}")
            }
        } catch (e: Exception) {
            // ✅ 예외 발생 시 로그 출력
            Log.e("ChatGPT", "API 요청 실패", e)
        }
    }


}
