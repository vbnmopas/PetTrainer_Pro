package org.techtown.multiwindow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class RecordActivity: AppCompatActivity() {

    lateinit var backButton : Button
    lateinit var btn3 : Button

    /** XML 변수 */
    private lateinit var audioRecordImageBtn: ImageButton
    private lateinit var audioRecordText: TextView

    /** 오디오 파일 관련 변수 */
    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_CODE = 21

    // 오디오 파일 녹음 관련 변수
    private var mediaRecorder: MediaRecorder? = null
    private var audioFileName: String? = null
    private var isRecording = false
    private var audioUri: Uri? = null

    // 오디오 파일 재생 관련 변수
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var playIcon: ImageView? = null

    /** 리사이클러뷰 */
    private lateinit var audioAdapter: AudioAdapter
    private val audioList = ArrayList<Uri>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record)

        backButton = findViewById<Button>(R.id.backButton)
        btn3 = findViewById<Button>(R.id.btn3)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }

        init()
    }


    // XML 변수 초기화 & 리사이클러뷰 설정
    private fun init() {
        audioRecordImageBtn = findViewById(R.id.audioRecordImageBtn)
        audioRecordText = findViewById(R.id.audioRecordText)

        audioRecordImageBtn.setOnClickListener {
            if (isRecording) {
                isRecording = false
                audioRecordImageBtn.setImageDrawable(getDrawable(R.drawable.ic_record))
                audioRecordText.text = "녹음 시작"
                stopRecording()
            } else {
                if (checkAudioPermission()) {
                    isRecording = true
                    audioRecordImageBtn.setImageDrawable(getDrawable(R.drawable.ic_record))
                    audioRecordText.text = "녹음 중"
                    startRecording()
                }
            }
        }

        // 리사이클러뷰 설정
        val audioRecyclerView: RecyclerView = findViewById(R.id.recyclerview)
        audioAdapter = AudioAdapter(this, audioList)
        audioRecyclerView.adapter = audioAdapter
        audioRecyclerView.layoutManager = LinearLayoutManager(this)

        // 아이템 클릭 리스너
        audioAdapter.setOnItemClickListener(object : AudioAdapter.OnIconClickListener {
            override fun onItemClick(view: View, position: Int) {
                val uriName = audioList[position].toString()
                val file = File(uriName)

                // 클릭된 뷰가 ImageView인지 확인 후 캐스팅
                val clickedIcon = view as? ImageView ?: return

                if (isPlaying) {
                    if (playIcon === view as ImageView) {
                        stopAudio()
                    } else {
                        stopAudio()
                        playIcon = clickedIcon
                        playAudio(file)
                    }
                } else {
                    playIcon = view as ImageView
                    playAudio(file)
                }
            }
        })


    }


    // 오디오 파일 권한 체크
    private fun checkAudioPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(this, recordPermission) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(recordPermission), PERMISSION_CODE)
            false
        }
    }


    // 녹음 시작
    private fun startRecording() {
        val recordPath = getExternalFilesDir("/")!!.absolutePath
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        audioFileName = "$recordPath/RecordExample_${timeStamp}_audio.mp4"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    // 녹음 종료
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            mediaRecorder = null
        }

        // 파일 경로를 Uri로 변환 후 리스트에 추가
        audioUri = Uri.parse(audioFileName)
        audioUri?.let { audioList.add(it) }
        audioAdapter.notifyDataSetChanged()
    }

    // 녹음 파일 재생
    private fun playAudio(file: File) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        playIcon?.setImageDrawable(getDrawable(R.drawable.ic_audio_pause))
        isPlaying = true

        mediaPlayer?.setOnCompletionListener {
            stopAudio()
        }
    }



    // 녹음 파일 중지
    private fun stopAudio() {
        playIcon?.setImageDrawable(getDrawable(R.drawable.ic_audio_play))
        isPlaying = false
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }



}