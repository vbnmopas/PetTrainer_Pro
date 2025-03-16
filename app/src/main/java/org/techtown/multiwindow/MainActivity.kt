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
//        edtshow2 = findViewById<TextView>(R.id.edtshow2)
//        edtshow3 = findViewById<TextView>(R.id.edtshow3)
//        txtin = findViewById<TextView>(R.id.txtin)
//        txtout = findViewById<TextView>(R.id.txtout)
//        txtmoney = findViewById<TextView>(R.id.txtmoney)
//        ttinout = findViewById<TextView>(R.id.ttinout)
        btn3 = findViewById<Button>(R.id.btn3)
        button1 = findViewById<Button>(R.id.button1)

        myHelper = myDBHelper(this)
        sqlDB = myHelper.readableDatabase
        var cursor: Cursor? = null
        cursor = sqlDB.rawQuery("SELECT * FROM MONEYdb;", null)


        //총수입, 총지출, 잔액 계산해서 textview에 출력하기 (메인 홈화면
       /* var cc = 0
        var cc2 = 0
        var cc3 = 0
        var aa: String? = ""
        var aa2: String? = ""
        val aa3: String
        while (cursor.moveToNext()) {
            //ginout가 1일때(데이터가 수입일때)
            if (cursor.getInt(2) == 1) {
                cc = cc + cursor.getInt(4)
                aa = Integer.toString(cc)
                txtin.text = aa
            } else {
                cc2 = cc2 + cursor.getInt(4)
                aa2 = Integer.toString(cc2)
                txtmoney.text = aa2
            }
        }

        cc3 = cc - cc2
        aa3 = Integer.toString(cc3)
        txtout.text = aa3*/




/*
        //!!!!!!!!!캘린더내용 주석!!!!!!!!!
        //캘린더 날짜를 누르면 실행
        mCalendarView = findViewById<CalendarView>(R.id.calendarView)
        mCalendarView.setOnDateChangeListener { calendarView, year, month, day ->
            val date = "$year/${month + 1}/$day"
            val ii1 = year.toString()
            val ii2 = (month + 1).toString()
            val ii3 = day.toString()
            val date3 = ii1 + ii2 + ii3
            Log.d(TAG, "onSelectDayChange: date: $date")


            sqlDB = myHelper.readableDatabase
            var cursor: Cursor
            cursor = sqlDB.rawQuery("SELECT * FROM MONEYdb WHERE gtt = '$date3';", null)

            var strtext = ""
            var strmoney = ""
            var strbtn = ""
            var strinout = ""

            var a: Int = 0
            var b: String = ""

            while (cursor.moveToNext()) {
                strtext += cursor.getString(0) + "\r\n"
                strmoney += cursor.getString(4) + "\r\n"
                a = cursor.getString(3).toInt()
                b = when (a) {
                    1 -> "용돈"
                    2 -> "월급"
                    3 -> "식비"
                    4 -> "교통비"
                    5 -> "통신비"
                    6 -> "공과금"
                    else -> "생필품"
                }
                strbtn += "$b\r\n"
            }

            sqlDB = myHelper.readableDatabase
            cursor = sqlDB.rawQuery("SELECT * FROM MONEYdb WHERE gtt = '$date3';", null)
            var ab: Int
            while (cursor.moveToNext()) {
                ab = cursor.getString(2).toInt()
                if (ab == 1) {
                    strinout += "수입\r\n"
                } else {
                    strinout += "지출\r\n"
                }
            }

            edtshow2.setText(strtext)
            edtshow3.setText(strmoney)
            edtshow.setText(strbtn)
            ttinout.setText(strinout)

            cursor?.close()
            sqlDB?.close()
        }
*/

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
//        val webSettings: WebSettings = webView.settings
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




//        -------------------------------------------------------------------------------
        //!!!!!!!!!캘린더내용 주석!!!!!!!!!
    //편집누르면 편집화면 이동
//        btn3.setOnClickListener {
//            //달력을 클릭하지 않고 편집버튼을 눌러서 편집화면으로 감, 갈때 date에 오늘날짜를 넣어준다.
//            if (date == "") {
//                var today = SimpleDateFormat("yyyy/m/dd").format(Date())
//                date = today
//                var today2 = SimpleDateFormat("yyyymdd").format(Date())
//                date3 = today2
//            }
//            var intent = Intent(applicationContext, AddActivity::class.java)
//            intent.putExtra("date", date)
//            intent.putExtra("date3", date3)
//            startActivity(intent)
//        }
//
//        //상세내역버튼눌러서 화면 이동
//        button1.setOnClickListener  {
//            var intent = Intent(applicationContext, DetailsActivity::class.java)
//            startActivity(intent)
//        }



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