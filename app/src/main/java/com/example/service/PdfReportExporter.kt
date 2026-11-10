package com.example.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.ComponentInterval
import com.example.model.MotoProfile
import com.example.model.ServiceRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PdfReportExporter(private val context: Context) {

    fun generateMaintenancePdf(
        profile: MotoProfile,
        intervals: List<ComponentInterval>,
        records: List<ServiceRecord>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 portrait size
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(20, 20, 28)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(216, 0, 108) // Neon magenta accent
            textSize = 12f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 40, 50)
            textSize = 10f
            isAntiAlias = true
        }

        val boldBodyPaint = Paint().apply {
            color = Color.rgb(20, 20, 30)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val headerBgPaint = Paint().apply {
            color = Color.rgb(240, 242, 248)
        }

        val alertPaint = Paint().apply {
            color = Color.rgb(217, 30, 54)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(220, 222, 230)
            strokeWidth = 1f
        }

        var y = 45f

        // Document Title & Branding
        canvas.drawText("MOTODASH - HISTORIAL Y CONTROL DE SERVICIOS", 40f, y, titlePaint)
        y += 18f
        canvas.drawText("COMPUTADORA DE VIAJE & MANTENIMIENTO PREVENTIVO", 40f, y, subtitlePaint)
        y += 20f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Vehicle Info Block
        val currentDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawRect(40f, y - 15f, 555f, y + 55f, headerBgPaint)

        canvas.drawText("Vehículo:", 50f, y, boldBodyPaint)
        canvas.drawText("${profile.brandModel} ('${profile.nickname}')", 130f, y, bodyPaint)

        canvas.drawText("Placa:", 320f, y, boldBodyPaint)
        canvas.drawText(profile.plateNumber, 370f, y, bodyPaint)

        y += 20f
        canvas.drawText("Odómetro Actual:", 50f, y, boldBodyPaint)
        canvas.drawText("${String.format("%.1f", profile.currentOdometerKm)} km", 150f, y, boldBodyPaint)

        canvas.drawText("Fecha de Emisión:", 320f, y, boldBodyPaint)
        canvas.drawText(currentDate, 420f, y, bodyPaint)

        y += 50f

        // Section 1: Estado de Componentes / Próximos Cambios
        canvas.drawText("ESTADO DE COMPONENTES Y PRÓXIMOS SERVICIOS", 40f, y, subtitlePaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // Table Header
        canvas.drawRect(40f, y - 12f, 555f, y + 6f, headerBgPaint)
        canvas.drawText("Componente", 50f, y, boldBodyPaint)
        canvas.drawText("Último Cambio", 190f, y, boldBodyPaint)
        canvas.drawText("Próximo Cambio", 300f, y, boldBodyPaint)
        canvas.drawText("Restante / Estado", 420f, y, boldBodyPaint)
        y += 18f

        intervals.forEach { interval ->
            canvas.drawText(interval.name, 50f, y, bodyPaint)
            canvas.drawText("${interval.lastServiceKm.toInt()} km", 190f, y, bodyPaint)
            canvas.drawText("${interval.nextServiceKm.toInt()} km", 300f, y, bodyPaint)

            val statusText = if (interval.remainingKm <= 300) {
                "! URGENTE (${interval.remainingKm.toInt()} km)"
            } else {
                "${interval.remainingKm.toInt()} km restantes"
            }
            val paintToUse = if (interval.remainingKm <= 300) alertPaint else bodyPaint
            canvas.drawText(statusText, 420f, y, paintToUse)

            y += 16f
            canvas.drawLine(40f, y - 4f, 555f, y - 4f, linePaint)
        }

        y += 25f

        // Section 2: Historial Detallado de Registros
        canvas.drawText("HISTORIAL DETALLADO DE MANTENIMIENTO", 40f, y, subtitlePaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 18f

        // History Table Header
        canvas.drawRect(40f, y - 12f, 555f, y + 6f, headerBgPaint)
        canvas.drawText("Fecha", 50f, y, boldBodyPaint)
        canvas.drawText("Odómetro", 130f, y, boldBodyPaint)
        canvas.drawText("Servicio / Detalle", 200f, y, boldBodyPaint)
        canvas.drawText("Costo", 470f, y, boldBodyPaint)
        y += 18f

        var totalSpent = 0.0
        records.forEach { record ->
            totalSpent += record.costMxn
            canvas.drawText(record.dateDisplay, 50f, y, bodyPaint)
            canvas.drawText("${record.odometerKm.toInt()} km", 130f, y, bodyPaint)

            val serviceTitle = if (record.notes.isNotEmpty()) {
                "${record.title} (${record.notes})"
            } else {
                record.title
            }
            val displayTitle = if (serviceTitle.length > 40) serviceTitle.substring(0, 37) + "..." else serviceTitle
            canvas.drawText(displayTitle, 200f, y, bodyPaint)

            canvas.drawText("$${String.format("%.2f", record.costMxn)} MXN", 470f, y, boldBodyPaint)

            y += 16f
            canvas.drawLine(40f, y - 4f, 555f, y - 4f, linePaint)
        }

        y += 20f
        canvas.drawText("Inversión Total en Servicios: $${String.format("%.2f", totalSpent)} MXN", 300f, y, subtitlePaint)

        y += 40f
        canvas.drawText("Reporte generado automáticamente desde MotoDash para ${profile.brandModel}.", 40f, y, bodyPaint)
        y += 14f
        canvas.drawText("Validez técnica certificada. Mantenga siempre la presión adecuada en neumáticos y niveles de lubricante.", 40f, y, bodyPaint)

        document.finishPage(page)

        // Save PDF to cache/reports directory
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, "Reporte_Mantenimiento_${profile.brandModel.replace(" ", "_")}.pdf")

            val fos = FileOutputStream(pdfFile)
            document.writeTo(fos)
            fos.close()
            document.close()
            pdfFile
        } catch (e: Throwable) {
            try {
                val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
                val fallbackFile = File(reportsDir, "Reporte_Mantenimiento_${profile.brandModel.replace(" ", "_")}.txt")
                fallbackFile.writeText(
                    "MotoDash - Reporte de Mantenimiento\n" +
                    "Modelo: ${profile.brandModel}\n" +
                    "Odómetro: ${profile.currentOdometerKm} km\n" +
                    "Fecha: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}\n" +
                    "Intervalos: ${intervals.size}\n" +
                    "Registros: ${records.size}\n"
                )
                fallbackFile
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun sharePdfReport(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Reporte de Mantenimiento MotoDash")
            putExtra(Intent.EXTRA_TEXT, "Adjunto reporte detallado de servicios y mantenimiento de mi moto.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(shareIntent, "Exportar / Compartir Reporte PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
