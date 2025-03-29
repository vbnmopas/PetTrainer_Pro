package org.techtown.multiwindow
import android.content.Context
import android.content.Intent
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

    lateinit var backButton : Button
    lateinit var AIbtn : Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatMsgAdapter
    private lateinit var btnSend: Button
    private lateinit var etMsg: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var chatMsgList: MutableList<ChatMsg>

//    val commandStats = intent.getSerializableExtra("commandStats") as? HashMap<String, Pair<Int, Int>> ?: mutableMapOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gpt)

//        val commandStats = intent.getSerializableExtra("commandStats") as? HashMap<String, Pair<Int, Int>> ?: mutableMapOf()

        backButton = findViewById<Button>(R.id.backButton)
        AIbtn = findViewById<Button>(R.id.AIbtn)

        backButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }



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

        // 급식 기록을 불러와서 챗봇에게 먼저 전송
        val feedRecord = getFeedRecords()
        if (feedRecord.isNotEmpty()) {
            sendInitialMessage(feedRecord)
        }

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

    // 저장된 급식 기록을 가져오는 함수
    private fun getFeedRecords(): String {
        val sharedPref = getSharedPreferences("FeederPrefs", Context.MODE_PRIVATE)
        val feedRecord = sharedPref.getString("feed_records", "급식 내역이 없습니다.") ?: "급식 내역이 없습니다."

        Log.d("GPTActivity", "불러온 급식 기록: $feedRecord")  // 여기서 로그 추가

        return feedRecord
    }

    // 저장된 훈련 기록을 가져오는 함수
    private fun getTrainRecords(): String {
        val sharedPref = getSharedPreferences("TrainingStats", Context.MODE_PRIVATE)
        val trainingStats = sharedPref.getString("train_records", "훈련 내역이 없습니다.") ?: "훈련 내역이 없습니다."

        Log.d("GPTActivity", "불러온 훈련 기록: $trainingStats")  // 여기서 로그 추가

        return trainingStats
    }

/*
    // 훈련 성공률 메시지 생성
    private fun buildTrainingStatsMessage(): String {

        val sb = StringBuilder()
        for ((command, stats) in commandStats) {
            val successRate = if (stats.first + stats.second > 0) {
                (stats.first.toFloat() / (stats.first + stats.second)) * 100
            } else 0f

            sb.appendLine("$command: ${"%.2f".format(successRate)}% 성공률")
        }
        return sb.toString()
    }
*/

    // 챗봇에게 초기 메시지를 보내는 함수
    private fun sendInitialMessage(feedRecord: String) {

        // 훈련 성공률을 포맷된 문자열로 변환
        val trainingStats = getTrainRecords()

        if (feedRecord.isNotEmpty()) {
            val feedRecord = getFeedRecords()

            Log.d("GPTActivity", "훈련 성공률 문자열: $trainingStats")

            val initialMsg = "다음은 강아지의 급식 기록입니다:\n$feedRecord\n " +
                    "다음은 강아지의 훈련 성공률입니다:\n$trainingStats\n" +
                    " 이 정보를 기반으로 오늘 강아지의 건강 상태를 분석해 주세요."

            Log.d("GPTActivity", "초기 메시지 전송: $initialMsg")  // 초기 메시지 로그

            // 초기 메시지를 실제 화면에는 보이지 않도록 하지 않음
            val chatMsg = ChatMsg(ChatMsg.ROLE_USER, initialMsg)

            // 여기에 화면에 메시지를 표시하지 않도록 하기 위해 RecyclerView에 추가하지 않음
            chatMsgList.add(chatMsg)

//            adapter.addChatMsg(chatMsg)
//            chatMsgList.add(chatMsg)

            progressBar.visibility = View.VISIBLE
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            // ✅ 별도 메시지 리스트를 생성하여 초기 메시지 전송
            lifecycleScope.launch {
                sendInitialMsgToChatGPT(initialMsg)
            }
        } else {
            Log.d("GPTActivity", "급식 기록이 비어 있습니다. 초기 메시지를 전송하지 않습니다.")
        }
    }

    // 초기 메시지 전송 함수
    private suspend fun sendInitialMsgToChatGPT(initialMessage: String) {
        val api = ApiClient.chatGPTApi

        val request = ChatGPTRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(ChatMsg(ChatMsg.ROLE_USER, initialMessage))  // 새로운 메시지 리스트 생성
        )

        Log.d("GPTActivity", "초기 메시지 전송 요청: $initialMessage")  // 요청 로그 추가

        try {
            Log.d("ChatGPT", "ChatGPT API 요청: $request")  // API 요청 로그

            val response = api.getChatResponse(request)

            if (response.isSuccessful && response.body() != null) {
                val chatResponse = response.body()!!.choices[0].message.content

                Log.d("ChatGPT", "ChatGPT 응답: $chatResponse")  // 응답 로그 추가

                val responseMsg = ChatMsg(ChatMsg.ROLE_ASSISTANT, chatResponse)
                adapter.addChatMsg(responseMsg)
                chatMsgList.add(responseMsg)

            } else {
                Log.e("ChatGPT", "응답 실패: ${response.code()} - ${response.message()}")  // 응답 실패 로그
            }
        } catch (e: Exception) {
            Log.e("ChatGPT", "API 요청 실패", e)  // 예외 로그
        } finally {
            progressBar.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    // ChatGPT에 사용자 메시지 전송
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
