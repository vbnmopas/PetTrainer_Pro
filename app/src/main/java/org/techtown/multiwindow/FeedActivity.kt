package org.techtown.multiwindow

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class FeedActivity  : AppCompatActivity() {

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

        // 🔹 SharedPreferences 초기화 (이걸 추가해야 오류 해결됨!)
        sharedPref = getSharedPreferences("FeederPrefs", Context.MODE_PRIVATE)

        // 🔹 이전 급식량 불러오기 (기본값: 10g)
        val lastFeedAmount = sharedPref.getInt("last_feed_amount", 10)
        seekBarAmount.progress = lastFeedAmount
        // 급식량 표시
        textViewAmount.text = "급식량: ${lastFeedAmount}g"
        textViewCurrentAmount.text = "현재 설정된 급식량: ${lastFeedAmount}g"  // 🔹 추가된 안내 문구



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
            showCompletionDialog()  // 완료 메시지 출력
        }


        // 예약 급식 버튼 클릭 이벤트
        btnSetSchedule.setOnClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val amount = seekBarAmount.progress
            sendFeedCommand("schedule", amount, hour, minute)
            showScheduleCompletionDialog()    // 완료 메시지 출력
        }
    }

    // MQTT 또는 HTTP로 명령 전송하는 함수
    private fun sendFeedCommand(mode: String, amount: Int, hour: Int = 0, minute: Int = 0) {
        val message = if (mode == "immediate") {
            "feed_now"
        } else {
            "schedule_feed:$hour:$minute:$amount"
        }

        // TODO: MQTT 또는 HTTP를 사용하여 ESP32로 메시지 전송
        Log.d("Feeder", "보낼 메시지: $message")

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
}