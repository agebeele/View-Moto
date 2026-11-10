package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import com.example.model.BikeGraphicStyle
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs
import kotlin.math.sqrt

class ImageVectorizationProcessor(private val context: Context) {

    /**
     * Processes motorcycle image into transparent PNG or Neon Vector Silhouette
     */
    fun processMotorcycleImage(
        sourceBitmap: Bitmap,
        style: BikeGraphicStyle,
        accentColorInt: Int
    ): Bitmap {
        val width = sourceBitmap.width
        val height = sourceBitmap.height

        return when (style) {
            BikeGraphicStyle.VECTOR_NEON_SILHOUETTE -> {
                generateNeonVectorSilhouette(sourceBitmap, accentColorInt)
            }
            BikeGraphicStyle.CONTOUR_OUTLINE -> {
                generateContourOutline(sourceBitmap, accentColorInt)
            }
            BikeGraphicStyle.TRANSPARENT_STUDIO_PNG -> {
                generateTransparentStudioCutout(sourceBitmap)
            }
        }
    }

    /**
     * Creates a transparent PNG by removing dark/solid backgrounds
     */
    fun generateTransparentStudioCutout(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample corner colors for background estimation
        val cornerColor = pixels[0]
        val cornerR = Color.red(cornerColor)
        val cornerG = Color.green(cornerColor)
        val cornerB = Color.blue(cornerColor)

        val threshold = 38.0

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Calculate distance to background color
            val dist = sqrt(
                ((r - cornerR) * (r - cornerR) +
                 (g - cornerG) * (g - cornerG) +
                 (b - cornerB) * (b - cornerB)).toDouble()
            )

            if (dist < threshold) {
                // Background pixel: set completely transparent
                pixels[i] = Color.TRANSPARENT
            } else if (dist < threshold + 25.0) {
                // Smooth edge anti-aliasing / feathering
                val alpha = (((dist - threshold) / 25.0) * 255).toInt().coerceIn(0, 255)
                pixels[i] = Color.argb(alpha, r, g, b)
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Vectorizes motorcycle into high-contrast neon glowing edge lines
     */
    fun generateNeonVectorSilhouette(source: Bitmap, accentColorInt: Int): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val outPixels = IntArray(width * height)

        val accentR = Color.red(accentColorInt)
        val accentG = Color.green(accentColorInt)
        val accentB = Color.blue(accentColorInt)

        // Sobel filter approximation for edge detection
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                val pLeft = pixels[idx - 1]
                val pRight = pixels[idx + 1]
                val pUp = pixels[idx - width]
                val pDown = pixels[idx + width]

                val lumLeft = (Color.red(pLeft) * 299 + Color.green(pLeft) * 587 + Color.blue(pLeft) * 114) / 1000
                val lumRight = (Color.red(pRight) * 299 + Color.green(pRight) * 587 + Color.blue(pRight) * 114) / 1000
                val lumUp = (Color.red(pUp) * 299 + Color.green(pUp) * 587 + Color.blue(pUp) * 114) / 1000
                val lumDown = (Color.red(pDown) * 299 + Color.green(pDown) * 587 + Color.blue(pDown) * 114) / 1000

                val dx = abs(lumRight - lumLeft)
                val dy = abs(lumDown - lumUp)
                val edgeGrad = (dx + dy).coerceIn(0, 255)

                if (edgeGrad > 32) {
                    val alpha = (edgeGrad * 1.5).toInt().coerceIn(120, 255)
                    outPixels[idx] = Color.argb(alpha, accentR, accentG, accentB)
                } else {
                    outPixels[idx] = Color.TRANSPARENT
                }
            }
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        return output
    }

    private fun generateContourOutline(source: Bitmap, accentColorInt: Int): Bitmap {
        val silhouette = generateNeonVectorSilhouette(source, accentColorInt)
        val width = silhouette.width
        val height = silhouette.height

        val composed = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composed)

        val tintPaint = Paint().apply {
            colorFilter = PorterDuffColorFilter(accentColorInt, PorterDuff.Mode.SRC_IN)
            isAntiAlias = true
        }

        canvas.drawBitmap(silhouette, 0f, 0f, tintPaint)
        return composed
    }

    /**
     * Loads bitmap from Uri or returns null
     */
    fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Saves processed bitmap to app internal storage and returns file path
     */
    fun saveProcessedBitmap(bitmap: Bitmap, filename: String = "custom_bike_vector.png"): String? {
        return try {
            val file = File(context.filesDir, filename)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
