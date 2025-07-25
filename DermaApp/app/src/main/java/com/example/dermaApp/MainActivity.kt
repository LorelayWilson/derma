package com.example.dermaApp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.canhub.cropper.*
import com.canhub.cropper.CropImageView
import com.example.dermaApp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DermaAppNavigator()
            }
        }
    }
}

sealed interface Screen {
    data object Main : Screen
    data class FullScreenImage(val imageUri: Uri) : Screen
    data class AnalysisScreen(val imageUri: Uri) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DermaAppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var imageUriAsync by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val onNavigateToAnalysis: (Uri) -> Unit = { uri ->
        currentScreen = Screen.AnalysisScreen(uri)
    }
    val onNavigateBackFromAnalysis: () -> Unit = {
        currentScreen = Screen.Main
    }
    val cropActivityLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            val croppedUri = result.uriContent
            if (croppedUri != null) {
                Log.d("CROP_RESULT", "Imagen recortada exitosamente por la librería: $croppedUri")
                imageUriAsync = croppedUri
                currentScreen = Screen.Main
                Toast.makeText(context, "Imagen recortada", Toast.LENGTH_SHORT).show()
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
                onShowFullScreenImage = { uriToShow ->
                    currentScreen = Screen.FullScreenImage(uriToShow)
                },
                onAnalyseClicked = { uriToAnalyse ->
                    if (uriToAnalyse != null) {
                        onNavigateToAnalysis(uriToAnalyse)
                    } else {
                        Toast.makeText(context, "No hay imagen para analizar", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        is Screen.FullScreenImage -> {
            FullScreenImage(
                imageUri = screen.imageUri,
                onNavigateBack = {
                    currentScreen = Screen.Main
                },
                onCropImage = { uriToCrop ->
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
        is Screen.AnalysisScreen -> {
            AnalysisResultScreen(
                imageUri = screen.imageUri,
                onNavigateBack = onNavigateBackFromAnalysis
            )
        }
    }
}

