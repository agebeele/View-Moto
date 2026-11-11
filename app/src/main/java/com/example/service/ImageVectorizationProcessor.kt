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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
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

    private val httpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    /**
     * Attempts to call RESTful Background Removal API (e.g. RemoveBG, ClipDrop, or Custom REST Endpoint).
     * Automatically falls back to high-precision local cutout if offline or no key is provided.
     */
    suspend fun removeBackgroundWithRestApi(
        sourceBitmap: Bitmap,
        apiEndpoint: String = "https://api.remove.bg/v1.0/removebg",
        apiKey: String = ""
    ): Bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (apiKey.isBlank()) {
            // Local high-precision studio cutout
            return@withContext extractTransparentMotorcyclePng(sourceBitmap, toleranceFraction = 0.38f, smoothEdges = true)
        }

        try {
            val byteStream = java.io.ByteArrayOutputStream()
            sourceBitmap.compress(Bitmap.CompressFormat.PNG, 95, byteStream)
            val imageBytes = byteStream.toByteArray()

            val mediaType = "image/png".toMediaTypeOrNull()
            val body = imageBytes.toRequestBody(mediaType)
            val requestBody = okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("image_file", "motorcycle.png", body)
                .addFormDataPart("size", "auto")
                .build()

            val request = okhttp3.Request.Builder()
                .url(apiEndpoint)
                .addHeader("X-Api-Key", apiKey)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBytes = response.body?.bytes()
                if (responseBytes != null && responseBytes.isNotEmpty()) {
                    val decoded = BitmapFactory.decodeByteArray(responseBytes, 0, responseBytes.size)
                    if (decoded != null) return@withContext decoded
                }
            }
        } catch (_: Exception) {}

        // Fallback to high-precision local cutout
        extractTransparentMotorcyclePng(sourceBitmap, toleranceFraction = 0.38f, smoothEdges = true)
    }

    /**
     * Creates a clean transparent PNG isolating the motorcycle from background.
     * Uses multi-point corner sampling and adjustable color distance tolerance.
     */
    fun extractTransparentMotorcyclePng(
        source: Bitmap,
        toleranceFraction: Float = 0.38f,
        smoothEdges: Boolean = true
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample background from all 4 corners + top/bottom edge centers
        val samplePoints = listOf(
            0,
            width - 1,
            (height - 1) * width,
            (height - 1) * width + (width - 1),
            width / 2,
            (height - 1) * width + (width / 2)
        )

        val bgSamples = samplePoints.map { idx ->
            val c = pixels[idx]
            Triple(Color.red(c), Color.green(c), Color.blue(c))
        }

        // Tolerance scaled between 15.0 and 120.0
        val baseThreshold = (toleranceFraction * 120.0).coerceIn(15.0, 110.0)
        val featherRange = if (smoothEdges) 22.0 else 6.0

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Calculate min distance to any background sample
            var minDist = Double.MAX_VALUE
            for (sample in bgSamples) {
                val dr = r - sample.first
                val dg = g - sample.second
                val db = b - sample.third
                val dist = sqrt((dr * dr + dg * dg + db * db).toDouble())
                if (dist < minDist) {
                    minDist = dist
                }
            }

            if (minDist < baseThreshold) {
                // Background pixel: completely transparent
                pixels[i] = Color.TRANSPARENT
            } else if (smoothEdges && minDist < baseThreshold + featherRange) {
                // Edge feathering to eliminate harsh pixel fringes
                val factor = ((minDist - baseThreshold) / featherRange).coerceIn(0.0, 1.0)
                val alpha = (factor * 255).toInt()
                pixels[i] = Color.argb(alpha, r, g, b)
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Backward-compatible default studio cutout
     */
    fun generateTransparentStudioCutout(source: Bitmap): Bitmap {
        return extractTransparentMotorcyclePng(source, toleranceFraction = 0.38f, smoothEdges = true)
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
