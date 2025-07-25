package com.example.dermaApp.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.util.DebugLogger
import com.example.dermaApp.R
import com.example.dermaApp.utils.createImageFileUri // Asumiendo que mueves la utilidad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DermaAppScreen(
    currentImageUri: Uri?,
    onImageCapturedOrUpdated: (Uri?) -> Unit,
    onShowFullScreenImage: (Uri) -> Unit
) {
    val context = LocalContext.current
    // imageUriToEdit no se usa en esta pantalla, podría eliminarse o moverse si es necesario en otro lado
    var imageUriForCamera by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val coilImageLoader = remember {
        ImageLoader.Builder(context)
            .logger(DebugLogger())
            .build()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
            if (isGranted) {
                Log.d("DermaApp", "Permiso de cámara CONCEDIDO")
            } else {
                Log.w("DermaApp", "Permiso de cámara DENEGADO")
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                Log.d(
                    "DermaApp",
                    "Imagen capturada exitosamente. URI original intentada por Coil: $imageUriForCamera"
                )
                onImageCapturedOrUpdated(imageUriForCamera)
            } else {
                Log.d("DermaApp", "Captura de imagen cancelada o fallida.")
                onImageCapturedOrUpdated(null)
            }
        }
    )

    // El resto de tu UI de DermaAppScreen
    // ... (Scaffold, Column, if (currentImageUri == null), etc.)
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.derma_app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.99f)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Black,
                                        0.95f to Color.Black,
                                        1.0f to Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = size.height
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        },
                    contentScale = ContentScale.FillBounds
                )
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (currentImageUri == null) {
                MensajePrincipal(
                    texto = "Analiza tu piel con una foto",
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            val newImageUri = createImageFileUri(context) // Usa la función de utilidad
                            if (newImageUri != null) {
                                imageUriForCamera = newImageUri
                                cameraLauncher.launch(newImageUri)
                            } else {
                                Toast.makeText(context, "Error al preparar la cámara", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    // ... otros modificadores y contenido del botón
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Abrir Cámara",
                        modifier = Modifier.size(75.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp)) // Espacio entre elementos

                PhotoInstructionsCard()

                Spacer(modifier = Modifier.height(8.dp)) // Espacio entre tarjetas

                DisclaimerCard()
            } else {
                Text(
                    text = "Imagen capturada:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            1.dp, Color.Black, RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            onShowFullScreenImage(currentImageUri)
                        }
                ) {
                    AsyncImage(
                        model = currentImageUri,
                        imageLoader = coilImageLoader,
                        contentDescription = "Imagen capturada por el usuario",
                        modifier = Modifier.wrapContentSize(),
                        contentScale = ContentScale.Fit,
                        placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                        error = painterResource(id = R.drawable.ic_launcher_background)
                    )
                    IconButton(
                        onClick = {
                            Log.d("DermaApp_DISMISS", "Dismissing image, calling onImageCapturedOrUpdated(null)")
                            onImageCapturedOrUpdated(null)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Descartar imagen",
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        Log.d("DermaApp", "Botón Analizar pulsado con URI: $currentImageUri")
                    },
                    modifier = Modifier.width(120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text("Analizar")
                }
            }
        }
    }
}

// Podrías tener estas como funciones privadas o en otro archivo de UI common
@Composable
fun MensajePrincipal(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.headlineSmall, // Un estilo prominente
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground, // Color que contraste con el fondo
        modifier = modifier
            .padding(vertical = 24.dp) // Añade algo de espacio vertical
    )
}

@Composable
fun PhotoInstructionsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Consejos para una buena foto:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "- Asegúrate de tener buena iluminación.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "- Enfoca bien la zona de la piel a analizar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "- Evita sombras y reflejos directos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "- La imagen debe ser clara y nítida.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer) // Color para destacar la advertencia
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Importante:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Esta aplicación es una herramienta de ayuda y no reemplaza el diagnóstico de un profesional médico. Consulta siempre a tu dermatólogo.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
