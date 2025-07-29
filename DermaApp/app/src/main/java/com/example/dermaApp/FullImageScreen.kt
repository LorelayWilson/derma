package com.example.dermaApp

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImage(
    imageUri: Uri,
    onNavigateBack: () -> Unit,
    onCropImage: (Uri) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Vista previa",
                            modifier = Modifier.offset(x = (-12).dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.fondo),
                contentDescription = "Fondo de pantalla completa",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier.fillMaxSize()
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { centroid, pan, zoom, rotation ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    val newOffsetX = offset.x + pan.x * scale
                                    val newOffsetY = offset.y + pan.y * scale
                                    offset = Offset(newOffsetX, newOffsetY)
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fondo_card),
                        contentDescription = "Fondo de imagen",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Imagen a pantalla completa",
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                2.dp,
                                Color.Black,
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y,
                            )
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onCropImage(imageUri) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    ) { Text("Recortar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground
                        )
                    ) {
                        Text("Centrar")
                    }
                }
            }
        }
    }
}