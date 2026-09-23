package com.agkomputech.shoeshop.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Result of one capture: the decoded bitmap plus where it was saved on disk. */
data class CapturedPhoto(val bitmap: Bitmap, val filePath: String)

/**
 * Takes one photo with the given ImageCapture use case, saves it into this app's
 * private storage (app/files/shoe_photos/), and returns both the decoded Bitmap
 * (for immediate embedding) and the saved file path (for ShoePhoto.imagePath).
 */
suspend fun ImageCapture.captureToAppStorage(
    context: Context,
    fileNamePrefix: String
): CapturedPhoto = suspendCoroutine { continuation ->
    val photoDir = File(context.filesDir, "shoe_photos").apply { mkdirs() }
    val photoFile = File(photoDir, "${fileNamePrefix}_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    this.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (bitmap == null) {
                    continuation.resumeWithException(IllegalStateException("Could not decode captured photo"))
                } else {
                    continuation.resume(CapturedPhoto(bitmap, photoFile.absolutePath))
                }
            }

            override fun onError(exception: ImageCaptureException) {
                continuation.resumeWithException(exception)
            }
        }
    )
}
