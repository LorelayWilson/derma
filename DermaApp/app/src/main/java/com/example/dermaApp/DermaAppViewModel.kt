package com.example.dermaApp

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DermaAppViewModel(application: Application): AndroidViewModel(application) {
    private val imageAnalyzer = ImageAnalyzer(application.applicationContext)
    private val _analysisResult = MutableStateFlow<List<AnalysisPrediction>>(emptyList())
    val analysisResult: StateFlow<List<AnalysisPrediction>> = _analysisResult.asStateFlow()
    private val _isLoadingAnalysis = MutableStateFlow(false)
    val isLoadingAnalysis: StateFlow<Boolean> = _isLoadingAnalysis.asStateFlow()

    fun performAnalysis(imageUri: Uri, rotation: Int) {
        viewModelScope.launch {
            _isLoadingAnalysis.value = true
            _analysisResult.value = emptyList()
            try {
                val predictions = withContext(Dispatchers.IO) {
                    imageAnalyzer.analyzeImage(imageUri, rotation)
                }
                _analysisResult.value = predictions
            } catch (e: Exception) {
                Log.e("ViewModel", "Error en el análisis de imagen", e)
                _analysisResult.value = emptyList()
            } finally {
                _isLoadingAnalysis.value = false
            }
        }

        fun onCleared() {
            super.onCleared()
            imageAnalyzer.close()
        }
    }
}