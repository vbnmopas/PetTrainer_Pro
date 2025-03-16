package org.techtown.multiwindow

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class AudioAdapter(
    private val context: Context,
    private val dataModels: ArrayList<Uri>
) : RecyclerView.Adapter<AudioAdapter.MyViewHolder>() {

    // 커스텀 리스너 인터페이스
    interface OnIconClickListener {
        fun onItemClick(view: View, position: Int)
    }

    private var listener: OnIconClickListener? = null

    // 리스너 객체를 설정하는 메서드
    fun setOnItemClickListener(listener: OnIconClickListener) {
        this.listener = listener
    }

    override fun getItemCount(): Int {
        return dataModels.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val uriName = dataModels[position].toString()
        val file = File(uriName)
        holder.audioTitle.text = file.name
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val audioBtn: ImageButton = itemView.findViewById(R.id.playBtn_itemAudio)
        val audioTitle: TextView = itemView.findViewById(R.id.audioTitle_itemAudio)

        init {
            audioBtn.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(it, pos)
                }
            }
        }
    }
}