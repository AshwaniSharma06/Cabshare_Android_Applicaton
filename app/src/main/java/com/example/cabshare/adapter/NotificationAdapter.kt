package com.example.cabshare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cabshare.R
import com.example.cabshare.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val onNotificationClick: (Notification) -> Unit) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private var notifications = listOf<Notification>()

    fun setNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvNotificationTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)

        fun bind(notification: Notification) {
            tvTitle.text = notification.title
            tvMessage.text = notification.message
            
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            tvTime.text = sdf.format(Date(notification.timestamp))

            unreadIndicator.visibility = if (notification.isRead) View.GONE else View.VISIBLE

            val iconRes = when (notification.type) {
                "trip_posted" -> android.R.drawable.ic_input_add
                "match_found" -> android.R.drawable.ic_menu_compass
                "trip_started" -> android.R.drawable.ic_media_play
                else -> android.R.drawable.ic_popup_reminder
            }
            ivIcon.setImageResource(iconRes)

            itemView.setOnClickListener { onNotificationClick(notification) }
        }
    }
}
