package com.example.dermaApp

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    analysisPredictions: List<AnalysisPrediction>,
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    val allDiseaseDataMap = remember {
        val map = getDiseaseInfoMap()
        Log.d("AnalysisResultScreen", "allDiseaseDataMap initialized (keys: ${map.keys})")
        map
    }
    val resultsToShow: List<DiseaseInfo> = remember(analysisPredictions, allDiseaseDataMap) {
        Log.d("AnalysisResultScreen", "REMEMBER BLOCK for resultsToShow - Calculating...")
        analysisPredictions.forEachIndexed { index, pred ->
            Log.d("AnalysisResultScreen", "  ViewModel Pred #${index}: ${pred.diseaseName}, model_prob=${pred.probability}")
        }
        val filteredPredictions = analysisPredictions.filter { prediction ->
            val modelProbability = prediction.probability
            val shouldKeep = modelProbability > 0.0005f
            Log.d("AnalysisResultScreen", "  Filtering prediction ${prediction.diseaseName}: model_prob=${modelProbability}, shouldKeep=$shouldKeep")
            shouldKeep
        }
        Log.d("AnalysisResultScreen", "  Filtered Predictions (model_prob > 0.0f, count: ${filteredPredictions.size})")
        val mappedResults = filteredPredictions.mapNotNull { prediction ->
            val diseaseInfoFromMap = allDiseaseDataMap[prediction.diseaseName]
            if (diseaseInfoFromMap == null) {
                Log.w("AnalysisResultScreen", "    -> NOT FOUND in allDiseaseDataMap for '${prediction.diseaseName}' (post-filter)")
                null
            } else {
                Log.i("AnalysisResultScreen", "    -> MAPPING (post-filter) '${prediction.diseaseName}'")
                diseaseInfoFromMap.copy(
                    probability = prediction.probability * 100
                )
            }
        }
        Log.d("AnalysisResultScreen", "  Mapped results (AFTER filter and *100, count: ${mappedResults.size}):")
        mappedResults.forEach { info ->
            Log.d("AnalysisResultScreen", "    Mapped for display: ${info.name}, display_prob=${info.probability}")
        }
        val sortedResults = mappedResults.sortedByDescending { it.probability }
        Log.d("AnalysisResultScreen", "  Sorted results (FINAL resultsToShow, count: ${sortedResults.size})")
        sortedResults
    }
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
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (resultsToShow.isEmpty()) {
          Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
          ) {
              Text("No se encontraron resultados con probabilidad significativa.")
          }
        } else {
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
                items(resultsToShow) { disease ->
                    ExpandableInfoCard(
                        title = disease.name,
                        probabilityText = "Probabilidad: %.1f%%".format(disease.probability),
                        mainContentColor = disease.colorIndicator,
                        expandedContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "Nivel de Riesgo: ${disease.riskLevel}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    disease.riskDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )
                                Text(
                                    "¿Qué es?",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    disease.whatIsDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )
                                Text(
                                    "¿Cómo tratarlo?",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    disease.howToTreatDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

