package org.techtown.multiwindow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatMsgAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var dataList: List<ChatMsg> = mutableListOf()

    // 데이터 리스트를 세팅하기 위한 메서드입니다.
    fun setDataList(dataList: List<ChatMsg>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    // 채팅 메시지가 추가되었을 때 어댑터에 반영해주기 위한 메서드입니다.
    fun addChatMsg(chatMsg: ChatMsg) {
        (dataList as MutableList).add(chatMsg)
        notifyItemInserted(dataList.size - 1)
    }

    // 각 아이템의 뷰타입 호출시 ChatMsg 클래스의 role에 따라 내 메시지는 0 / 챗봇의 메시지는 1을 반환하도록 오버라이드 합니다.
    override fun getItemViewType(position: Int): Int {
        return if (dataList[position].role == ChatMsg.ROLE_USER) 0 else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // 뷰타입이 0이면 MyChatViewHolder를 반환
        return if (viewType == 0) {
            MyChatViewHolder(inflater.inflate(R.layout.item_my_chat, parent, false))
        } else {
            // 아니면 BotChatViewHolder 반환
            BotChatViewHolder(inflater.inflate(R.layout.item_bot_chat, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMsg = dataList[position]
        if (chatMsg.role == ChatMsg.ROLE_USER) {
            (holder as MyChatViewHolder).setMsg(chatMsg)
        } else {
            (holder as BotChatViewHolder).setMsg(chatMsg)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    // 내가 보낸 메시지를 띄우기 위한 뷰홀더입니다.
    inner class MyChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tv_msg)

        fun setMsg(chatMsg: ChatMsg) {
            tvMsg.text = chatMsg.content
        }
    }

    // 챗봇의 메시지를 띄우기 위한 뷰홀더입니다.
    inner class BotChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMsg: TextView = itemView.findViewById(R.id.tv_msg)

        fun setMsg(chatMsg: ChatMsg) {
            tvMsg.text = chatMsg.content
        }
    }
}
