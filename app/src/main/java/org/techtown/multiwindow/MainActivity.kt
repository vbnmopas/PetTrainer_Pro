package org.techtown.multiwindow


import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout

class MainActivity : AppCompatActivity() {

    lateinit var baseLayout : LinearLayout
    lateinit var button1 : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "배경색 바꾸기"

        baseLayout = findViewById<LinearLayout>(R.id.baseLayout)
        button1 = findViewById<Button>(R.id.button1)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        var mInflater = menuInflater
        mInflater.inflate(R.menu.menu1, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.itemRed -> {
                baseLayout.setBackgroundColor(Color.RED)
                return true
            }

            R.id.itemGreen -> {
                baseLayout.setBackgroundColor(Color.GREEN)
                return true
            }

            R.id.itemBlue -> {
                baseLayout.setBackgroundColor(Color.BLUE)
                return true
            }

            R.id.subRotate -> {
                button1.rotation = 45f
                return true
            }

            R.id.subSize -> {
                button1.scaleX = 2f
                return true
            }
        }

        return false
    }
}