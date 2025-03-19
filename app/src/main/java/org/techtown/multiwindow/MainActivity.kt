package org.techtown.multiwindow


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var btn3 : Button
    lateinit var button1 : Button

    //캘린더 변수 주석
    lateinit var mCalendarView: CalendarView

    //카메라 변수 선언
    private lateinit var cameraExecutor: ExecutorService;
    private val CAMERA_PERMISSION_CODE = 100

    // 웹뷰 선언
    lateinit var webView: WebView

    //버튼 선언
    lateinit var webview_btn : Button
    lateinit var recordBtn : Button
    lateinit var trainBtn : Button
    lateinit var foodBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            try {
                val serverIp = "43.201.77.16" // 여기에 MQTT 서버 IP 입력
                val address = InetAddress.getByName(serverIp)
                val reachable = address.isReachable(3000) // 3초 동안 핑 테스트

                if (reachable) {
                    Log.d("PING", "$serverIp is reachable!")
                } else {
                    Log.e("PING", "$serverIp is NOT reachable!")
                }
            } catch (e: Exception) {
                Log.e("PING", "Error: ${e.message}")
            }
        }.start()


        //---------------------------------------------------------------

        btn3 = findViewById<Button>(R.id.btn3)
//        button1 = findViewById<Button>(R.id.button1)

        recordBtn = findViewById<Button>(R.id.recordBtn)
        recordBtn.setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

        trainBtn = findViewById<Button>(R.id.trainBtn)
        trainBtn.setOnClickListener {
            val intent = Intent(this, TrainActivity::class.java)
            startActivity(intent)
        }

        foodBtn = findViewById(R.id.foodBtn)
        foodBtn.setOnClickListener{
            //새페이지 이동이 아니라 작은 창 띄워서 해결하고싶음

        }




        webview_btn = findViewById<Button>(R.id.WebView_btn)
        webview_btn.setOnClickListener {
            val intent = Intent(applicationContext, WebViewActivity::class.java)
            //웹사이트로 연결
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"))

            startActivity(intent)
        }


        //웹뷰 연결
        webView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()


        // 웹뷰 설정 (JavaScript 활성화)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW  // HTTPS + HTTP 섞여도 허용
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)


        // Flask 서버의 영상 스트리밍 URL 입력 (IP 주소 수정 필요)
        webView.loadUrl("https://192.168.0.6:5000/video")
//        webView.loadUrl("https://www.youtube.com")


    }

}