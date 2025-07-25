package com.example.dermaApp.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createImageFileUri(context: Context): Uri? {
    try {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        // Asegúrate de que tu FileProvider esté correctamente configurado en AndroidManifest.xml
        // y en res/xml/file_paths.xml
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Coincide con el authorities de tu Manifest
            imageFile
        )
    } catch (ex: IOException) {
        // Error creando el archivo
        ex.printStackTrace()
        return null
    }
}
   