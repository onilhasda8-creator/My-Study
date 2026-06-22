package com.studyplanner.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.studyplanner.MainActivity
import com.studyplanner.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_TASKS    = "study_planner_tasks"
        const val CHANNEL_ID_SESSIONS = "study_planner_sessions"

        const val NOTIFICATION_TASK_DUE     = 1001
        const val NOTIFICATION_SESSION_DONE = 1002
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_TASKS,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts for tasks that are due soon"
                }
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_SESSIONS,
                    "Study Sessions",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Notifications about completed study sessions"
                }
            )
        }
    }

    /** Show a notification for a task due today/overdue */
    fun showTaskDueNotification(taskId: Long, taskTitle: String, subjectName: String) {
        if (!hasNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TASKS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Task due: $taskTitle")
            .setContentText("$subjectName · Open Study Planner to review")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_TASK_DUE + taskId.toInt(), notification)
    }

    /** Show a notification when a study session is saved */
    fun showSessionSavedNotification(subjectName: String, durationMinutes: Int) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SESSIONS)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Study session saved!")
            .setContentText("$subjectName · ${DateUtils.formatMinutes(durationMinutes)}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_SESSION_DONE, notification)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
