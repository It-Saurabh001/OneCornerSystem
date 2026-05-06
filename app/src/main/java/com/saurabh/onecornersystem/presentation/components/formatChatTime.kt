package com.saurabh.onecornersystem.presentation.components
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


fun formatChatTime(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.DAY_OF_YEAR) == messageTime.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        }
        now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR) -> {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(date)
        }
        else -> {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
}

fun formatMessageTime(timestamp: Timestamp?): String {
    return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(timestamp?.toDate())
}