package com.lifeos.modules.lifeos_physicalmedia.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Singleton

@Singleton
class ImageCacheService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheDir: File by lazy {
        File(context.filesDir, "physical_covers").apply { mkdirs() }
    }

    suspend fun downloadAndCacheImage(imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .build()

            val request = Request.Builder()
                .url(imageUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko)")
                .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                .build()

            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null
            val bytes = body.bytes()

            val contentType = response.header("Content-Type")
            if (contentType?.contains("html") == true) return@withContext null

            val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
            val originalBitmap = try {
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } catch (e: Exception) {
                return@withContext null
            }

            val maxDimension = 400
            val scale = minOf(
                maxDimension.toFloat() / originalBitmap.width,
                maxDimension.toFloat() / originalBitmap.height
            ).coerceAtMost(1f)

            val width = (originalBitmap.width * scale).toInt()
            val height = (originalBitmap.height * scale).toInt()
            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            } else {
                originalBitmap
            }

            if (scaledBitmap != originalBitmap) originalBitmap.recycle()

            val file = File(cacheDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            scaledBitmap.recycle()

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun deleteCachedImage(localPath: String?) {
        localPath ?: return
        try {
            File(localPath).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
