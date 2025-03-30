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
import kotlin.math.log


class TrainActivity : AppCompatActivity() {

    lateinit var backButton : Button
    lateinit var AIbtn : Button

//    private val serverUrl = "http://192.168.180.214:5000/send" // Flask 서버 IP와 엔드포인트 수정
    private val serverUrl = "http://192.168.0.23:5000/send"


    lateinit var sitBtn: Button
    lateinit var bodylowerBtn: Button

    // 성공/실패 카운트
    private var successCount = 0
    private var failureCount = 0

    // 명령어별 (성공 횟수, 실패 횟수)
    private val commandStats = mutableMapOf<String, Pair<Int, Int>>()

    private lateinit var sharedPref: SharedPreferences  // SharedPreferences 선언

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

        // SharedPreferences 초기화 (이걸 추가해야 오류 해결됨)
        sharedPref = getSharedPreferences("TrainingStats", Context.MODE_PRIVATE)

        // 저장된 훈련 기록 로그로 출력
        printStoredTrainingRecords()
        updatePieChart() // 초기 차트 업데이트
    }

    private fun sendMessage(message: String) {
        Thread {
            try {
                // 메시지 전송 로그
                Log.d("HTTP", "보내는 메시지: $message")

                // URL 객체로 초기화
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

                // 명령어별 성공/실패 기록
                val currentStats = commandStats[message] ?: Pair(0, 0)
                val newStats = if (result == "성공") {
                    Pair(currentStats.first + 1, currentStats.second)
                } else {
                    Pair(currentStats.first, currentStats.second + 1)
                }
                commandStats[message] = newStats

                // 훈련 결과를 SharedPreferences에 저장
                saveCommandStats()

                // UI 업데이트 (메인 스레드에서 실행)
                runOnUiThread {
                    findViewById<TextView>(R.id.resultTextView).text = messageText
                    updatePieChart() // 차트 업데이트
                }

                conn.disconnect()
            } catch (e: Exception) {
                Log.e("HTTP", "HTTP Exception: ${e.message}")
            }
        }.start()
    }


    // 훈련 결과 저장
    private fun saveCommandStats() {
        val sharedPref = getSharedPreferences("TrainingStats", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // commandStats를 저장하는 방법
        val statsString = commandStats.entries.joinToString("\n") {
            val totalAttempts = it.value.first + it.value.second // 총 시도 횟수
            "${it.key}: ${it.value.first}/$totalAttempts" // "성공횟수/총시도횟수" 형식
        }

        editor.putString("train_records", statsString)
        editor.apply() // 저장
        Log.d("SharedPreferences", "저장된 훈련 기록: $statsString")
    }

    // 1. 성공률 계산 메서드
    private fun calculateSuccessRates(): Map<String, Float> {
        val successRates = mutableMapOf<String, Float>()

        val trainRecords = sharedPref.getString("train_records", null)

        if (!trainRecords.isNullOrEmpty()) {
            commandStats.clear() // 기존 데이터 초기화
            trainRecords.split("\n").forEach { record ->
                val parts = record.split(":")
                if (parts.size == 2) {
                    val command = parts[0].trim()
                    val stats = parts[1].split("/").map { it.trim() }

                    // stats 배열의 길이가 2인지 확인
                    if (stats.size == 2) {
                        try {
                            val success = stats[0].toInt()
                            val failure = stats[1].toInt()

                            // 성공과 실패 횟수를 기록
                            commandStats[command] = Pair(success, failure)
                        } catch (e: NumberFormatException) {
                            // 숫자로 변환할 수 없는 경우 예외 처리
                            Log.e("TrainingRecords", "숫자로 변환할 수 없는 값이 있습니다: ${stats.joinToString(", ")}")
                        }
                    }
                }
            }

            // 성공률 계산
            for ((command, stats) in commandStats) {
                val totalAttempts = stats.first + stats.second
                if (totalAttempts > 0) {
                    val successRate = (stats.first.toFloat() / totalAttempts) * 100
                    successRates[command] = successRate
                }
            }
        }
        // **디버깅 로그 추가 (성공률 출력)**
        Log.d("SuccessRates", "계산된 성공률: $successRates")
        return successRates
    }

    // 2. 파이 차트에 성공률을 반영하여 업데이트하는 메서드
    private fun updatePieChart() {
        val successRates = calculateSuccessRates()  // 성공률 계산

        Log.d("SuccessRates", "전달받은 성공률: $successRates")

        val pieChart = findViewById<PieChart>(R.id.commandPieChart)
        val entries = mutableListOf<PieEntry>()

        // 성공률에 따른 차트 데이터 추가
        successRates.forEach { (command, successRate) ->
            entries.add(PieEntry(successRate, command))
            Log.d("PieChartData", "Command: $command, Success Rate: $successRate%")
        }

        // 📌 성공률 텍스트 업데이트 코드 추가
        val successRateTextView = findViewById<TextView>(R.id.successRateTextView)
        if (successRates.isNotEmpty()) {
            val avgSuccessRate = successRates.values.average() // 평균 성공률 계산
            successRateTextView.text = "성공률: %.2f%%".format(avgSuccessRate)
            Log.d("SuccessRateTextView", "성공률 업데이트됨: ${successRateTextView.text}")
        } else {
            successRateTextView.text = "성공률: 0.00%"
        }

        // 차트 데이터 설정
        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "훈련 성공률")
            val colors = listOf(Color.GREEN, Color.RED, Color.YELLOW, Color.CYAN) // 색상 리스트
            dataSet.colors = colors.take(entries.size)  // entries.size에 맞게 색상을 할당
            dataSet.valueTextSize = 14f

            val pieData = PieData(dataSet)
            pieChart.data = pieData

            // 📌 차트에 들어갈 데이터 로그 확인
            for (entry in entries) {
                Log.d("PieChartData파이차트임", "Chart Entry - Label: ${entry.label}, Value: ${entry.value}")
            }

            pieChart.setUsePercentValues(false)
            pieChart.legend.isEnabled = false // 범례 비활성화
            pieChart.invalidate() // 차트 새로고침
        } else {
            Log.d("PieChartData", "차트 데이터가 없습니다. 차트 갱신하지 않음.")
        }
    }

    // 저장된 훈련 기록을 로그로 출력하는 메서드
    private fun printStoredTrainingRecords() {
        val trainRecords = sharedPref.getString("train_records", null)

        if (!trainRecords.isNullOrEmpty()) {
            // 저장된 훈련 기록 출력
            Log.d("TrainingRecords", "저장된 훈련 기록:\n$trainRecords")
        } else {
            Log.d("TrainingRecords", "저장된 훈련 기록이 없습니다.")
        }
    }
}



