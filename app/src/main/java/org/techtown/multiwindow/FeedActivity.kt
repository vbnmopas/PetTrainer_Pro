package org.techtown.multiwindow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class FeedActivity  : AppCompatActivity() {

    //버튼 변수 선언
    lateinit var backButton : Button
    lateinit var AIbtn : Button

    private lateinit var radioGroup: RadioGroup
    private lateinit var instantFeedLayout: LinearLayout
    private lateinit var scheduleFeedLayout: LinearLayout
    private lateinit var btnFeedNow: Button
    private lateinit var btnSetSchedule: Button
    private lateinit var timePicker: TimePicker
    private lateinit var seekBarAmount: SeekBar
    private lateinit var textViewAmount: TextView
    private lateinit var textViewCurrentAmount: TextView  // 🔹 추가된 텍스트뷰 (현재 급식량 표시)

    private lateinit var sharedPref: SharedPreferences  // 🔹 SharedPreferences 선언

    private lateinit var textViewFeedHistory: TextView // 🔹 내역을 표시할 텍스트 뷰 추가



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)


        // UI 요소 연결
        radioGroup = findViewById(R.id.radioGroup)
        instantFeedLayout = findViewById(R.id.instantFeedLayout)
        scheduleFeedLayout = findViewById(R.id.scheduleFeedLayout)
        btnFeedNow = findViewById(R.id.btnFeedNow)
        btnSetSchedule = findViewById(R.id.btnSetSchedule)
        timePicker = findViewById(R.id.timePicker)
        seekBarAmount = findViewById(R.id.seekBarAmount)
        textViewAmount = findViewById(R.id.textViewAmount)
        // 🔹 반드시 findViewById로 초기화!
        textViewCurrentAmount = findViewById(R.id.textViewCurrentAmount1)

        textViewFeedHistory = findViewById(R.id.textViewInstantFeedRecord) // 🔹 내역 텍스트 뷰 초기화

        // 🔹 SharedPreferences 초기화 (이걸 추가해야 오류 해결됨)
        sharedPref = getSharedPreferences("FeederPrefs", Context.MODE_PRIVATE)

        // 앱 시작 시 급식 내역 불러오기
        loadFeedHistory()


        // 🔹 이전 급식량 불러오기 (기본값: 10g)
        val lastFeedAmount = sharedPref.getInt("last_feed_amount", 10)
        seekBarAmount.progress = lastFeedAmount
        // 급식량 표시
        textViewAmount.text = "급식량: ${lastFeedAmount}g"
        textViewCurrentAmount.text = "현재 설정된 급식량: ${lastFeedAmount}g"  // 🔹 추가된 안내 문구


        backButton = findViewById<Button>(R.id.backButton)
        AIbtn = findViewById<Button>(R.id.AIbtn)


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        AIbtn.setOnClickListener {
            val intent = Intent(this, GPTActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }




        // 라디오 버튼 변경 이벤트 (즉시/예약 UI 전환)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioInstant) {
                instantFeedLayout.visibility = View.VISIBLE
                scheduleFeedLayout.visibility = View.GONE

                // 즉시 급식이 선택된 경우 텍스트로 급식량을 화면에 표시
                val currentFeedAmount = seekBarAmount.progress
                textViewCurrentAmount.text = "현재 설정된 급식량: ${currentFeedAmount}g"
            } else {
                instantFeedLayout.visibility = View.GONE
                scheduleFeedLayout.visibility = View.VISIBLE
            }
        }


        // SeekBar(급식량 설정) 이벤트
        seekBarAmount.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewAmount.text = "급식량: ${progress}g"
                textViewCurrentAmount.text = "현재 설정된 급식량: ${progress}g"  // 🔹 변경된 급식량 반영
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                saveFeedAmount(seekBar?.progress ?: 10)  // 🔹 급식량 저장
            }
        })

        // 즉시 급식 버튼 클릭 이벤트
        btnFeedNow.setOnClickListener {
            val currentFeedAmount = seekBarAmount.progress  // 🔹 현재 설정된 급식량 가져오기
            Toast.makeText(this, "${currentFeedAmount}g을 배식합니다.", Toast.LENGTH_SHORT).show()
            sendFeedCommand("immediate", currentFeedAmount)  // 🔹 최신 급식량 반영
            saveFeedRecord("immediate", currentFeedAmount)  // 급식 기록 저장
            showCompletionDialog()  // 완료 메시지 출력
            loadFeedHistory()  // 최신 급식 내역을 표시
        }


        // 예약 급식 버튼 클릭 이벤트
        btnSetSchedule.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amount = seekBarAmount.progress
            sendFeedCommand("schedule", amount, hour, minute)
            saveFeedRecord("schedule", amount, hour, minute)  // 예약 급식 기록 저장
            showScheduleCompletionDialog()    // 완료 메시지 출력
            loadFeedHistory()  // 최신 급식 내역을 표시
        }
    }

    private fun sendFeedCommand(command: String, amount: Int, hour: Int = 0, minute: Int = 0) {
        Thread {
            try {
                Log.d("HTTP", "보내는 메시지: command=$command, amount=$amount, hour=$hour, minute=$minute")

                // URL 객체로 초기화
                val url = URL("http://192.168.180.214:5000/feed")  // 서버의 '/feed' 엔드포인트
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                // JSON 메시지 준비
                val jsonPayload = if (command == "immediate") {
                    """{"time": "immediate", "amount": $amount}"""  // 즉시 급식
                } else {
                    val scheduledTime = String.format("%02d:%02d", hour, minute)
                    """{"time": "$scheduledTime", "amount": $amount}"""  // 예약 급식
                }

                val outputStream: OutputStream = conn.outputStream
                outputStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()

                // 서버 응답 받기
                val responseCode = conn.responseCode
                val responseMessage = conn.inputStream.bufferedReader().use { it.readText() }

                Log.d("HTTP", "Response Code: $responseCode, Response: $responseMessage")

                runOnUiThread {
                    if (responseCode == 200) {
                        Toast.makeText(applicationContext, "배식이 완료되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, "배식 실패. 서버 오류 발생!", Toast.LENGTH_SHORT).show()
                    }
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("HTTP", "HTTP Exception: ${e.message}")
                runOnUiThread {
                    Toast.makeText(applicationContext, "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }



    // 배식 완료 메시지 출력하는 함수
    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("배식 완료")
            .setMessage("배식이 완료되었습니다!")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()  // 다이얼로그 닫기
            }
            .show()
    }

    // 예약 완료 다이얼로그 메시지 출력하는 함수
    private fun showScheduleCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("예약 완료")
            .setMessage("예약이 완료되었습니다!")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()  // 다이얼로그 닫기
            }
            .show()
    }

    // 🔹 마지막 급식량 저장 함수
    private fun saveFeedAmount(amount: Int) {
        sharedPref.edit().putInt("last_feed_amount", amount).apply()
    }

    // 급식 내역 저장
    private fun saveFeedRecord(mode: String, amount: Int, hour: Int = 0, minute: Int = 0) {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = timeFormat.format(Date())  // 현재 시간 (현재 날짜와 시간)

        // 예약 급식의 경우 오늘 날짜와 예약 시간을 포맷하여 저장
        val record = if (mode == "schedule") {
            // 오늘 날짜를 얻어오기
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // 예약 시간 포맷 (예: 14:30)
            val scheduledTime = String.format("%02d:%02d", hour, minute)
            // "오늘 날짜 14:30" 형식으로 예약 내역을 만듦
            "급식 방식: $mode, 급식량: ${amount}g, 예약된 급식 시간: $todayDate $scheduledTime"
        } else {
            "급식 방식: $mode, 급식량: ${amount}g, 급식 시간: $currentTime"
        }

        val existingRecords = sharedPref.getString("feed_records", "")
        val newRecords = if (existingRecords.isNullOrEmpty()) {
            record
        } else {
            "$existingRecords\n$record"
        }

        sharedPref.edit().putString("feed_records", newRecords).apply()
        Log.d("FeedPreferences", "급식 기록 저장됨: Mode=$mode, Amount=$amount, Time=$currentTime")
    }

    // 급식 내역 불러오기
    private fun loadFeedHistory() {
        val feedHistory = sharedPref.getString("feed_records", "급식 내역이 없습니다.") ?: "급식 내역이 없습니다."
        textViewFeedHistory.text = feedHistory // 내역을 텍스트뷰에 표시
    }

}