package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_MAINTENANCE_ID = "motodash_maintenance_urgent"
        const val CHANNEL_TPMS_ID = "motodash_tpms_alerts"
        const val NOTIFICATION_MAINTENANCE_TAG = "urgent_service"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val maintenanceChannel = NotificationChannel(
                CHANNEL_MAINTENANCE_ID,
                "Alertas Urgentes de Mantenimiento",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios de servicio próximo y componentes críticos de la moto"
                enableVibration(true)
            }

            val tpmsChannel = NotificationChannel(
                CHANNEL_TPMS_ID,
                "Alertas de Seguridad TPMS",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de presión baja o sobrecalentamiento en neumáticos"
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(maintenanceChannel)
            notificationManager.createNotificationChannel(tpmsChannel)
        }
    }

    fun sendMaintenanceAlert(
        title: String,
        message: String,
        notificationId: Int = 1001
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_MAINTENANCE_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("MotoDash: $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            // Handled when POST_NOTIFICATIONS is not yet granted on Android 13+
        }
    }

    fun sendTpmsWarning(tireName: String, currentPsi: Double) {
        sendMaintenanceAlert(
            title = "¡Alerta TPMS Crítica!",
            message = "Presión anormal en $tireName: ${String.format("%.1f", currentPsi)} PSI. Deténgase en un lugar seguro.",
            notificationId = 1002
        )
    }
}
