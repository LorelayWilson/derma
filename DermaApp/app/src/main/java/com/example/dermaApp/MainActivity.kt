package com.example.dermaApp

import android.Manifest
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.ImageLoader
import coil.util.DebugLogger
import com.example.dermaApp.ui.theme.MyApplicationTheme
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

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
                    CoroutineScope(Dispatchers.Main).launch {
                        val croppedUri = cropImageAndGetUri(context, uriToCrop)
                        if (croppedUri != null) {
                            try {
                                Log.d("NAV_CROP_TEST", "Intentando cargar bitmap desde cropped URI: $croppedUri")
                                val testBitmap = uriToBitmap(context, croppedUri)
                                if (testBitmap != null) {
                                    Log.d("NAV_CROP_TEST", "Bitmap cargado exitosamente desde cropped URI. Ancho: ${testBitmap.width}")
                                } else {
                                    Log.e("NAV_CROP_TEST", "FALLO al cargar bitmap desde cropped URI.")
                                }
                            } catch (e: Exception) {
                                Log.e("NAV_CROP_TEST", "Excepción al cargar bitmap desde cropped URI", e)
                            }
                            Toast.makeText(context, "Imagen recortada", Toast.LENGTH_SHORT).show()
                            onImageProcessed(croppedUri)
                        } else {
                            Toast.makeText(context, "Error al recortar", Toast.LENGTH_SHORT).show()
                            onImageProcessed(uriToCrop)
                        }
                    }
                }
            )
        }
    }
}
suspend fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("UriToBitmap", "Error converting Uri to Bitmap", e)
            null
        }
    }
}

suspend fun cropImageAndGetUri(context: Context, sourceUri: Uri): Uri? {
    return withContext(Dispatchers.IO) {
        val originalBitmap = uriToBitmap(context, sourceUri) ?: return@withContext null
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        val targetWidth = (originalWidth * 0.8).toInt()
        val targetHeight = (originalHeight * 0.8).toInt()
        val x = (originalWidth - targetWidth) / 2
        val y = (originalHeight - targetHeight) / 2
        if (targetWidth <= 0 || targetHeight <= 0) {
            Log.e("Crop", "Dimensiones de recorte inválidas")
            return@withContext sourceUri
        }
        val croppedBitmap = try {
            Bitmap.createBitmap(originalBitmap, x, y, targetWidth, targetHeight)
        } catch (e: Exception) {
            Log.e("Crop", "Error al crear bitmap recortado", e)
            return@withContext sourceUri
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "CROPPED_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var newFile: File? = null
        try {
            newFile = File.createTempFile(imageFileName, ".jpg", storageDir)
            val fos = FileOutputStream(newFile)
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            Log.d("Crop", "Imagen recortada guardada en: ${newFile.absolutePath}")
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                newFile
            )
        } catch (e: IOException) {
            Log.e("Crop", "Error en bloque try-catch de guardado/obtención de URI", e)
            newFile?.delete()
            return@withContext null
        } finally {
            if (!croppedBitmap.isRecycled) {
                //croppedBitmap.recycle()
            }
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
    //var imageUri by remember { mutableStateOf<Uri?>(null) }
    //var imageUriAsync by remember { mutableStateOf<Uri?>(null) }
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
    val context = LocalContext.current
    val coilImageLoader = remember { ImageLoader.Builder(context).logger(DebugLogger()).build() }
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = "Fondo de pantalla completa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = uri,
                    imageLoader = coilImageLoader,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, color = Color.Black, RoundedCornerShape(8.dp)),
                    contentDescription = "Imagen pantalla completa",
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onCrop(uri) },
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
                        onClick = onDismiss,
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