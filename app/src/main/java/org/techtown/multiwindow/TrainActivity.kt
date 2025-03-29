package org.techtown.multiwindow

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


class TrainActivity : AppCompatActivity() {

    lateinit var backButton : Button
    lateinit var AIbtn : Button

    private val serverUrl = "http://172.30.1.88:5000/send" // Flask 서버 IP와 엔드포인트 수정

    lateinit var sitBtn: Button
    lateinit var bodylowerBtn: Button

    //성공/실패 카운트
    private var successCount = 0
    private var failureCount = 0

    // 명령어별 (성공 횟수, 실패 횟수)
    private val commandStats = mutableMapOf<String, Pair<Int, Int>>()

    private lateinit var sharedPref: SharedPreferences  // 🔹 SharedPreferences 선언



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        sitBtn = findViewById(R.id.sitBtn)
        sitBtn.setOnClickListener {
            sendMessage("앉기")
        }

        bodylowerBtn = findViewById(R.id.bodylowerBtn)
        bodylowerBtn.setOnClickListener {
            sendMessage("엎드리기")
        }

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

        // 🔹 SharedPreferences 초기화 (이걸 추가해야 오류 해결됨)
        sharedPref = getSharedPreferences("TrainingStats", Context.MODE_PRIVATE)

    }


    private fun sendMessage(message: String) {
        Thread {
            try {
                // 메시지 전송 로그
                Log.d("HTTP", "보내는 메시지: $message")

                val url = URL(serverUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val jsonPayload = """{"message": "$message"}"""
                val outputStream: OutputStream = conn.outputStream
                outputStream.write(jsonPayload.toByteArray(Charsets.UTF_8))
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                val responseMessage = conn.inputStream.bufferedReader().use { it.readText() }

                // 응답 로그 출력
                Log.d("HTTP", "Response Code: $responseCode, Response: $responseMessage")

                val jsonResponse = JSONObject(responseMessage)
                val result = jsonResponse.getString("result")
                val messageText = jsonResponse.getString("message")

                // 서버로부터 받은 메시지 출력
                Log.d("HTTP", "서버 응답 메시지: $messageText")

                // ✅ 명령어별 성공/실패 기록
                val currentStats = commandStats[message] ?: Pair(0, 0)
                val newStats = if (result == "성공") {
                    Pair(currentStats.first + 1, currentStats.second)
                } else {
                    Pair(currentStats.first, currentStats.second + 1)
                }
                commandStats[message] = newStats

                // 훈련 결과를 SharedPreferences에 저장
                saveCommandStats()

                // ✅ UI 업데이트 (메인 스레드에서 실행)
                runOnUiThread {
                    findViewById<TextView>(R.id.resultTextView).text = messageText
                    updatePieChart() // 🔹 차트 업데이트 추가!
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("HTTP", "HTTP Exception: ${e.message}")
            }
        }.start()
    }

    //훈련 결과 저장
    private fun saveCommandStats() {
        val sharedPref = getSharedPreferences("TrainingStats", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // commandStats를 저장하는 방법
        // ✅ 성공 횟수 / 총 시도 횟수 형태로 저장
        val statsString = commandStats.entries.joinToString("\n") {
            val totalAttempts = it.value.first + it.value.second // 총 시도 횟수
            "${it.key}: ${it.value.first}/$totalAttempts" // "성공횟수/총시도횟수" 형식
        }

        editor.putString("train_records", statsString)
        editor.apply() // 저장
        Log.d("SharedPreferences", "저장된 훈련 기록: $statsString")
    }

    private fun updatePieChart() {
        val pieChart = findViewById<PieChart>(R.id.commandPieChart)
        val entries = mutableListOf<PieEntry>()

        // 명령어별 성공률 데이터 추가
        for ((command, stats) in commandStats) {
            val successRate = if (stats.first + stats.second > 0) {
                (stats.first.toFloat() / (stats.first + stats.second)) * 100
            } else 0f

            entries.add(PieEntry(successRate, command))
        }

        val dataSet = PieDataSet(entries, "훈련 성공률")
        dataSet.colors = listOf(Color.GREEN, Color.BLUE, Color.RED) // 색상 설정
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieChart.data = pieData
        pieChart.invalidate() // 차트 새로고침
    }
}


