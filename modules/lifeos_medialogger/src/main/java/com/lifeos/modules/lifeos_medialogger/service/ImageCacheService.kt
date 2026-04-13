package com.lifeos.modules.lifeos_medialogger.service

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
        File(context.filesDir, "media_covers").apply { mkdirs() }
    }

    suspend fun downloadAndCacheImage(imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            println("ImageCacheService: Downloading image from $imageUrl")
            
            val client = OkHttpClient.Builder()
                .followRedirects(true)
                .build()
            
            val request = Request.Builder()
                .url(imageUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko)")
                .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                .build()
            
            val response = client.newCall(request).execute()
            
            val finalUrl = response.request.url.toString()
            if (finalUrl != imageUrl) {
                println("ImageCacheService: Redirected to $finalUrl")
            }
            
            val contentType = response.header("Content-Type")
            println("ImageCacheService: Content-Type: $contentType")
            
            if (!response.isSuccessful) {
                println("ImageCacheService: Bad response ${response.code}")
                return@withContext null
            }
            
            val body = response.body
            if (body == null) {
                println("ImageCacheService: Empty response body")
                return@withContext null
            }
            
            val contentLength = body.contentLength()
            println("ImageCacheService: Content-Length: $contentLength")
            
            val bytes = body.bytes()

            if (contentType?.contains("html") == true) {
                println("ImageCacheService: Got HTML instead of image!")
                val str = String(bytes).take(500)
                println("ImageCacheService: Got: $str")
                return@withContext null
            }

            val source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
            val originalBitmap = try {
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } catch (e: Exception) {
                println("ImageCacheService: Failed to decode bitmap: ${e.message}")
                return@withContext null
            }
            
            println("ImageCacheService: Original size ${originalBitmap.width}x${originalBitmap.height}")

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

            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }

            val fileName = "${UUID.randomUUID()}.jpg"
            val file = File(cacheDir, fileName)
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            scaledBitmap.recycle()

            println("ImageCacheService: Saved to ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            println("ImageCacheService: Error downloading $imageUrl: ${e.message}")
            e.printStackTrace()
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