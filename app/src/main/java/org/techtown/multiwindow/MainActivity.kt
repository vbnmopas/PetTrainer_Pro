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
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    lateinit var edtshow : TextView
    lateinit var edtshow2 : TextView
    lateinit var edtshow3 : TextView
    lateinit var date : String
    lateinit var ii1 : String
    lateinit var ii2 : String
    lateinit var ii3 : String
    lateinit var date3 : String
    lateinit var txtin : TextView
    lateinit var txtout : TextView
    lateinit var txtmoney : TextView
    lateinit var ttinout : TextView
    lateinit var btn3 : Button
    lateinit var button1 : Button

    lateinit var sqlDB : SQLiteDatabase
    lateinit var myHelper : myDBHelper

    //캘린더 변수 주석
    lateinit var mCalendarView: CalendarView

    //카메라 변수 선언
    private lateinit var cameraExecutor: ExecutorService;
    private val CAMERA_PERMISSION_CODE = 100

    // 웹뷰 선언
    lateinit var webView: WebView


    lateinit var webview_btn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //title = "가계부"

//        edtshow = findViewById<TextView>(R.id.edtshow)
////        edtshow2 = findViewById<TextView>(R.id.edtshow2)
////        edtshow3 = findViewById<TextView>(R.id.edtshow3)
////        txtin = findViewById<TextView>(R.id.txtin)
////        txtout = findViewById<TextView>(R.id.txtout)
////        txtmoney = findViewById<TextView>(R.id.txtmoney)
////        ttinout = findViewById<TextView>(R.id.ttinout)
        btn3 = findViewById<Button>(R.id.btn3)
        button1 = findViewById<Button>(R.id.button1)

        myHelper = myDBHelper(this)
        sqlDB = myHelper.readableDatabase
        var cursor: Cursor? = null
        cursor = sqlDB.rawQuery("SELECT * FROM MONEYdb;", null)









//        ---------------------------------------------------------------------------------------

        //카메라 실행
//        val previewView: PreviewView = findViewById(R.id.previewView)
//        startCamera(previewView)
//
//        checkAndRequestPermissions(previewView) // 📌 권한 요청을 먼저 실행

//        cameraExecutor = Executors.newSingleThreadExecutor()


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

    class myDBHelper(context: Context) : SQLiteOpenHelper(context, "MONEYdb", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE MONEYdb (gName TEXT, gtt INTEGER, ginout INTEGER, gbtnn INTEGER, gNumber INTEGER);")
            // gName: 내용 gtt: 날짜 ginout: 수입인지 지출인지(수입:1, 지출:2) gbtnn: 항목버튼 gNumber: 금액
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS MONEYdb")
            onCreate(db)
        }


    }

    //카메라 메서드
    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera: Camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "카메라 실행 실패", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun checkAndRequestPermissions(previewView: PreviewView) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera(previewView) // 📌 권한이 있으면 카메라 실행
        }
    }



}