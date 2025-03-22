package org.techtown.multiwindow

import android.content.Intent
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
    lateinit var btn3 : Button

    private val serverUrl = "http://192.168.0.6:5000/send" // Flask 서버 IP와 엔드포인트 수정

    lateinit var sitBtn: Button
    lateinit var bodylowerBtn: Button
    lateinit var foodBtn: Button

    //성공/실패 카운트
    private var successCount = 0
    private var failureCount = 0

    // 명령어별 (성공 횟수, 실패 횟수)
    private val commandStats = mutableMapOf<String, Pair<Int, Int>>()


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
        btn3 = findViewById<Button>(R.id.btn3)


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

    }


    private fun sendMessage(message: String) {
        Thread {
            try {
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

                Log.d("HTTP", "Response Code: $responseCode, Response: $responseMessage")

                val jsonResponse = JSONObject(responseMessage)
                val result = jsonResponse.getString("result")
                val messageText = jsonResponse.getString("message")

                // ✅ 명령어별 성공/실패 기록
                val currentStats = commandStats[message] ?: Pair(0, 0)
                val newStats = if (result == "성공") {
                    Pair(currentStats.first + 1, currentStats.second)
                } else {
                    Pair(currentStats.first, currentStats.second + 1)
                }
                commandStats[message] = newStats

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
