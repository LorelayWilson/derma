package com.example.dermaApp.navigation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.dermaApp.ui.screens.DermaAppFullScreenImage
import com.example.dermaApp.ui.screens.DermaAppScreen
import com.canhub.cropper.CropImageContractOptions

sealed class Screen {
    object Main : Screen()
    data class FullScreenImage(val uri: Uri, val isCropped: Boolean = false) : Screen()
}

@Composable
fun DermaAppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var imageUriAsync by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val onShowFullScreenImage: (Uri) -> Unit = { uri ->
        currentScreen = Screen.FullScreenImage(uri)
    }
    val onDismissFullScreenImage: () -> Unit = {
        val window = (context as? Activity)?.window
        window?.let {
            val insetsController = WindowCompat.getInsetsController(it, it.decorView)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        currentScreen = Screen.Main
    }
    val onImageProcessed: (Uri?) -> Unit = { newUri ->
        Log.d("DermaApp_NAV_UPDATE", "Updating imageUriAsync to: $newUri")
        imageUriAsync = newUri
        currentScreen = Screen.Main
    }

    val cropActivityLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                Log.d("CROP_RESULT", "Imagen recortada exitosamente por la librería: $croppedUri")
                Toast.makeText(context, "Imagen recortada", Toast.LENGTH_SHORT).show()
                onImageProcessed(croppedUri)
            } else {
                Log.e("CROP_RESULT", "cropResult Uri is null even though result is successful.")
                Toast.makeText(context, "Error al obtener URI recortada", Toast.LENGTH_SHORT).show()
            }
        } else {
            val exception = result.error
            Log.e("CROP_ERROR", "Error al recortar imagen con la librería", exception)
            Toast.makeText(context, "Error al recortar: ${exception?.message}", Toast.LENGTH_LONG).show()
        }
    }

    when (val screen = currentScreen) {
        is Screen.Main -> {
            DermaAppScreen(
                currentImageUri = imageUriAsync,
                onImageCapturedOrUpdated = { newUri ->
                    imageUriAsync = newUri
                },
                onShowFullScreenImage = onShowFullScreenImage
            )
        }
        is Screen.FullScreenImage -> {
            DermaAppFullScreenImage(
                uri = screen.uri,
                onDismiss = onDismissFullScreenImage,
                onCrop = { uriToCrop ->
                    val cropOptions = CropImageContractOptions(
                        uri = uriToCrop,
                        cropImageOptions = CropImageOptions(
                            guidelines = CropImageView.Guidelines.ON
                        )
                    )
                    cropActivityLauncher.launch(cropOptions)
                }
            )
        }
    }
}
   