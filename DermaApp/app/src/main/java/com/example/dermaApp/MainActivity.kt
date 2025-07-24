package com.example.dermaApp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.util.DebugLogger
import com.canhub.cropper.CropImageView
import com.example.dermaApp.ui.theme.MyApplicationTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

sealed class Screen {
    object Main : Screen()
    data class FullScreenImage(val uri: Uri, val isCropped: Boolean = false) : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DermaAppNavigator() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }
    var imageUriAsync by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val onShowFullScreenImage: (Uri) -> Unit = { uri ->
        currentScreen = Screen.FullScreenImage(uri)
    }
    val onDismissFullScreenImage: () -> Unit = {
        currentScreen = Screen.Main
    }
    val onImageProcessed: (Uri?) -> Unit = { newUri ->
        Log.d("DermaApp_NAV_UPDATE", "Updating imageUriAsync to: $newUri")
        imageUriAsync = newUri
        currentScreen = Screen.Main
    }
    val cropActivityLauncher = rememberLauncherForActivityResult(
        contract = com.canhub.cropper.CropImageContract()
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
                //onDismiss = onDismissFullScreenImage,
                onDismiss = {
                  val window = (context as? Activity)?.window
                  window?.let {
                      val insetsController = WindowCompat.getInsetsController(it, it.decorView)
                      insetsController.show(WindowInsetsCompat.Type.systemBars())
                  }
                  onDismissFullScreenImage()
                },
                onCrop = { uriToCrop ->
                    val cropOptions = com.canhub.cropper.CropImageContractOptions(
                        uri = uriToCrop,
                        cropImageOptions = com.canhub.cropper.CropImageOptions(
                            guidelines = CropImageView.Guidelines.ON
                        )
                    )
                    cropActivityLauncher.launch(cropOptions)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DermaAppScreen(
    currentImageUri: Uri?,
    onImageCapturedOrUpdated: (Uri?) -> Unit,
    onShowFullScreenImage: (Uri) -> Unit) {
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }
    var imageUriToEdit by remember { mutableStateOf<Uri?>(null) }
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
                    texto = "Tome una foto",
                    modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
                )
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            val newImageUri = createImageFileUri(context)
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
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, Color.Black, CircleShape),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Abrir Cámara",
                        modifier = Modifier.size(75.dp)
                    )
                }
                PhotoInstruccictionsCard()
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
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp)
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
    if (showFullScreenImage && imageUriToEdit != null) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = { showFullScreenImage = false },
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.fondo),
                        contentDescription = "Fondo de pantalla completa",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = imageUriToEdit,
                        imageLoader = coilImageLoader,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, color = Color.Black, RoundedCornerShape(8.dp)),
                        contentDescription = "Imagen pantalla completa",
                        contentScale = ContentScale.FillBounds
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                showFullScreenImage = false
                                Toast.makeText(context, "Lanzar Recorte", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text("Recortar")
                        }
                        Button(
                            onClick = { showFullScreenImage = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DermaAppFullScreenImage(uri: Uri, onDismiss: () -> Unit, onCrop: (Uri) -> Unit) {
    //val context = LocalContext.current
    val window = (LocalView.current.context as Activity).window
    SideEffect {
        window?.let {
            val insetsController = WindowCompat.getInsetsController(it, it.decorView)
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss, enabled = true)
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = "Fondo de pantalla completa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen a pantalla completa",
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = {
                        Log.d("FullScreen", "Botón Recortar presionado para URI: $uri")
                        onCrop(uri)
                    }) {
                        Text("Recortar")
                    }
                    Button(onClick = onDismiss) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

fun createImageFileUri(context: Context): Uri? {
    var imageFile: File?
    try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs()
        }
        imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    } catch (ex: IOException) {
        Log.e("FileCreation", "Error creating image file", ex)
        return null
    }
    return if (imageFile != null) {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    } else {
        null
    }
}


@Composable
fun MensajePrincipal(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Composable
fun PhotoInstruccictionsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box (modifier = Modifier.fillMaxWidth(0.9f) ) {
            Image(
                painter = painterResource(id = R.drawable.fondo_card),
                contentDescription = "Fondo Instrucciones",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Instrucciones para la foto:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =  "• Asegúrese de que la zona esté bien iluminada.\n" +
                            "• Enfoque bien la imagen.\n" +
                            "• Evite sombras sobre el área de interés.\n" +
                            "• Mantenga el teléfono estable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box (modifier = Modifier.fillMaxWidth(0.9f)) {
            Image(
                painter = painterResource(id = R.drawable.fondo_card),
                contentDescription = "Fondo Disclaimer",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Información Importante Sobre DermaApp:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =  "Recuerda que esta app es una herramienta informativa y no reemplaza la opinión de un médico. Si tienes alguna preocupación o necesitas un diagnóstico, por favor, consulta a un profesional de la salud.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 20.sp
                )
            }
        }
    }
}