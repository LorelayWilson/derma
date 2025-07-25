package com.example.dermaApp

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisResultScreen(
    imageUri: Uri,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo),
            contentDescription = "Fondo de análisis",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
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
                            "Resultados del Análisis",
                                modifier = Modifier.offset(x = (-12).dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen analizada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                ExpandableInfoCard(
                    title = "Dermatitis Seborreica",
                    probabilityText = "Alta Probabilidad (75%)",
                    mainContentColor = Color(0xFFE53935),
                    expandedContent = {
                        Column {
                            Text(
                                "Descripción:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "La dermatitis seborreica es una afección común de la piel que afecta principalmente el cuero cabelludo. " +
                                        "Causa manchas escamosas, piel enrojecida y caspa persistente.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Síntomas Comunes:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "- Descamación (caspa) en el cuero cabelludo, cabello, cejas, barba o bigote.\n" +
                                        "- Manchas de piel grasosa cubiertas con escamas blancas o amarillas.\n" +
                                        "- Picazón.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            val recommendations = listOf(
                "Hidratación Profunda" to "Aplica una crema hidratante rica en ceramidas y ácido hialurónico dos veces al día, mañana y noche.",
                "Protección Solar Rigurosa" to "Usa protector solar de amplio espectro SPF 50+ diariamente, reaplicando cada 2-3 horas si hay exposición directa al sol, incluso en días nublados.",
                "Limpieza Facial Suave" to "Utiliza un limpiador facial suave, sin sulfatos ni fragancias, para no resecar ni irritar la piel. Limpia el rostro máximo dos veces al día.",
                "Evitar Irritantes" to "Identifica y evita productos cosméticos o de cuidado personal que contengan alcohol, fragancias fuertes o alérgenos conocidos.",
                "Dieta y Agua" to "Mantén una dieta equilibrada rica en antioxidantes (frutas, verduras) y bebe suficiente agua durante el día para una hidratación interna.",
                "No Exfoliar en Exceso" to "Limita la exfoliación a 1-2 veces por semana con productos suaves para no dañar la barrera cutánea.",
                "Manejo del Estrés" to "Practica técnicas de relajación, ya que el estrés puede exacerbar algunas condiciones de la piel.",
                "Ropa Adecuada" to "Si tienes piel sensible en el cuerpo, opta por ropa de algodón y evita tejidos sintéticos que puedan causar irritación.",
                "Humidificador" to "Considera usar un humidificador en ambientes secos, especialmente durante el invierno, para mantener la humedad de la piel.",
                "Parches de Prueba" to "Antes de usar un producto nuevo en toda la cara, realiza una prueba de parche en una pequeña área (ej. detrás de la oreja o en el antebrazo) durante 24-48h.",
                "Consulta Dermatológica" to "Programa una consulta con un dermatólogo para un diagnóstico preciso y un plan de tratamiento personalizado si los síntomas persisten o empeoran."
            )
            items(recommendations) { (title, description) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

