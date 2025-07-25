package com.example.dermaApp.ui.screens

// ... Tus imports necesarios (Popup, AsyncImage, Button, etc.)
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.util.DebugLogger
import com.example.dermaApp.R

@Composable
fun DermaAppFullScreenImage(
    uri: Uri,
    onDismiss: () -> Unit,
    onCrop: (Uri) -> Unit
) {
    val context = LocalContext.current
    val coilImageLoader = remember {
        ImageLoader.Builder(context)
            .logger(DebugLogger()) // Considera quitar el DebugLogger para producción
            .build()
    }

    // El WindowInsetsController ya se maneja en el onDismiss del Navigator,
    // por lo que no necesitas SideEffect aquí para ocultarlo, sino para mostrarlo al salir.
    // Para ocultarlo al entrar, podrías necesitar un SideEffect si no lo haces globalmente.

    Popup(
        alignment = Alignment.Center,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false // Para ocupar toda la pantalla
        )
    ) {
        Surface( // Surface para el fondo del Popup
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f) // Fondo semi-transparente o sólido
        ) {
            Box(modifier = Modifier.fillMaxSize()) { // Contenedor para la imagen de fondo si la quieres dentro del popup
                Image(
                    painter = painterResource(id = R.drawable.fondo), // Considera si este fondo es necesario aquí
                    contentDescription = "Fondo de pantalla completa",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center, // Centrar contenido
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = uri,
                    imageLoader = coilImageLoader,
                    modifier = Modifier
                        .weight(1f) // Que ocupe el espacio disponible
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, color = Color.Black, RoundedCornerShape(8.dp)),
                    contentDescription = "Imagen pantalla completa",
                    contentScale = ContentScale.Fit // O FillBounds si prefieres
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onCrop(uri) }, // Usar la uri actual para recortar
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary, // Ejemplo de color
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text("Recortar")
                    }
                    Button(
                        onClick = onDismiss, // Llama al onDismiss proporcionado
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary, // Ejemplo de color
                            contentColor = MaterialTheme.colorScheme.onSecondary
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
   