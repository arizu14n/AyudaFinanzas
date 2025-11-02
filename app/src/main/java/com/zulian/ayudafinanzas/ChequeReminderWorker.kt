package com.zulian.ayudafinanzas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zulian.ayudafinanzas.data.check.Check
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChequeReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ChequeReminderWorker"
        private const val NOTIFICATION_CHANNEL_ID = "CHEQUE_REMINDERS"
        private const val DAYS_BEFORE_EXPIRATION = 3
        private const val TARGET_ESTADO = "En cartera"
    }

    override suspend fun doWork(): Result {
        return try {
            val response: Response<List<Check>> = AuthApiClient.getApiService(applicationContext).getMyCheques().execute()

            if (response.isSuccessful) {
                val cheques = response.body() ?: emptyList()
                val upcomingCheques = findUpcomingCheques(cheques)
                if (upcomingCheques.isNotEmpty()) {
                    sendNotification(upcomingCheques)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun findUpcomingCheques(cheques: List<Check>): List<Check> {
        val upcoming = mutableListOf<Check>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val reminderDate = Calendar.getInstance()

        val chequesEnCartera = cheques.filter { it.estado == TARGET_ESTADO }

        for (cheque in chequesEnCartera) {
            try {
                val expirationDateStr = cheque.fechaEmision
                val expirationDate = dateFormat.parse(expirationDateStr)
                
                reminderDate.time = expirationDate
                reminderDate.add(Calendar.DAY_OF_YEAR, -DAYS_BEFORE_EXPIRATION)

                if (today.after(reminderDate) && today.before(Calendar.getInstance().apply { time = expirationDate })) {
                    upcoming.add(cheque)
                }
            } catch (e: Exception) {
                // Ignorar cheques con formato de fecha inválido
            }
        }
        return upcoming
    }

    private fun sendNotification(cheques: List<Check>) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Recordatorios de Cheques",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones para cheques a punto de vencer."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationTitle = if (cheques.size == 1) {
            "Recordatorio de Vencimiento de Cheque"
        } else {
            "${cheques.size} cheques están por vencer"
        }

        val notificationText = cheques.joinToString(separator = "\n") {
            "Cheque N° ${it.nro} vence el ${it.fechaEmision}."
        }

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wallet)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
