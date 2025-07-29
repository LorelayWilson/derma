package com.example.dermaApp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


data class AnalysisPrediction(val diseaseName: String, val probability: Float)

class ImageAnalyzer(
    private val context: Context,
    private val modelName: String = "efficientnetb0_derma9_quant.tflite",
    private val labelsName: String = "labels.txt" // Añade para que sea configurable
) {
    private var interpreter: Interpreter? = null
    private lateinit var inputTensorShape: IntArray
    private lateinit var outputTensorShape: IntArray
    private lateinit var inputDataType: DataType
    private lateinit var outputDataType: DataType
    private var labels: List<String> = listOf()

    init {
        setupInterpreter()
        loadLabels()
    }

    private fun loadLabels() {
        try {
            labels = FileUtil.loadLabels(context, labelsName)
            Log.d("ImageAnalyzer", "Labels cargadas correctamente: $labels")
            if (::outputTensorShape.isInitialized && outputTensorShape.isNotEmpty()) {
                val numClassesFromModel = outputTensorShape.last()
                if (labels.size != numClassesFromModel) {
                    Log.w("ImageAnalyzer", "¡Advertencia! El número de etiquetas (${labels.size}) no coincide con el número de clases de salida del modelo ($numClassesFromModel).")
                }
            }
        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "Error al cargar labels '$labelsName'.", e)
            labels = emptyList()
        }
    }

    private fun setupInterpreter() {
        try {
            val model = FileUtil.loadMappedFile(context, modelName)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            val inputTensor = interpreter!!.getInputTensor(0)
            inputTensorShape = inputTensor.shape()
            inputDataType = inputTensor.dataType()
            val outputTensor = interpreter!!.getOutputTensor(0)
            outputTensorShape = outputTensor.shape()
            outputDataType = outputTensor.dataType()
        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "Error al inicializar TensorFlow Lite Interpreter.", e)
            interpreter = null
        }
    }
    fun analyzeImage(imageUri: Uri, rotationDegrees: Int): List<AnalysisPrediction> {
        if (interpreter == null) {
            Log.e("ImageAnalyzer", "Interpreter no inicializado. No se puede analizar.")
            return emptyList()
        }
        if (labels.isEmpty()) {
            Log.e("ImageAnalyzer", "Labels no cargadas. No se puede analizar.")
            return emptyList()
        }
        if (!::inputTensorShape.isInitialized || inputTensorShape.isEmpty()) {
            Log.e("ImageAnalyzer", "Input tensor shape no inicializado.")
            return emptyList()
        }
        val bitmap = uriToBitmap(imageUri) ?: return emptyList()
        try {
            val tensorImage = TensorImage(inputDataType)
            val inputHeight = inputTensorShape[1]
            val inputWidth = inputTensorShape[2]
            val imageProcessorBuilder = ImageProcessor.Builder()
                .add(ResizeOp(inputHeight, inputWidth, ResizeOp.ResizeMethod.BILINEAR))
                .add(Rot90Op(-rotationDegrees / 90))
            tensorImage.load(bitmap)
            val processedImage = imageProcessorBuilder.build().process(tensorImage)
            val inputBuffer = processedImage.buffer
            val outputElementCount = outputTensorShape.fold(1) { acc, i -> acc * i }
            val outputBufferSize = outputElementCount * outputDataType.byteSize()
            val outputBuffer = ByteBuffer.allocateDirect(outputBufferSize).order(ByteOrder.nativeOrder())
            Log.d("ImageAnalyzer", "Ejecutando inferencia con Interpreter...")
            interpreter!!.run(inputBuffer, outputBuffer)
            Log.d("ImageAnalyzer", "Inferencia completada.")
            outputBuffer.rewind()
            val predictions = mutableListOf<AnalysisPrediction>()
            val numClasses = outputTensorShape.last()
            if (labels.size != numClasses) {
                Log.e("ImageAnalyzer", "Error crítico: El número de etiquetas (${labels.size}) no coincide con las clases del modelo ($numClasses). Se detiene el postprocesamiento.")
                return predictions.apply { add(AnalysisPrediction("Error: Discrepancia Etiquetas/Clases", 0f)) }
            }
            if (outputDataType == DataType.FLOAT32) {
                val probabilities = FloatArray(numClasses)
                outputBuffer.asFloatBuffer().get(probabilities)
                for (i in probabilities.indices) {
                    predictions.add(AnalysisPrediction(labels[i], probabilities[i]))
                }
            } else if (outputDataType == DataType.UINT8 || outputDataType == DataType.INT8) {
                val outputTensorDetails = interpreter!!.getOutputTensor(0)
                val quantizationParams = outputTensorDetails.quantizationParams()
                val scale = quantizationParams.scale
                val zeroPoint = quantizationParams.zeroPoint
                Log.d("ImageAnalyzer", "Output es cuantizado. Scale: $scale, ZeroPoint: $zeroPoint")
                for (i in 0 until numClasses) {
                    val value = if (outputDataType == DataType.UINT8) {
                        outputBuffer.get(i).toInt() and 0xFF
                    } else {
                        outputBuffer.get(i).toInt()
                    }
                    val dequantizedValue = scale * (value - zeroPoint)
                    predictions.add(AnalysisPrediction(labels[i], dequantizedValue))
                }
            } else {
                Log.e("ImageAnalyzer", "Tipo de dato de salida no soportado para postprocesamiento: $outputDataType")
            }
            Log.d("ImageAnalyzer", "Predicciones (Interpreter): $predictions")
            return predictions.sortedByDescending { it.probability }

        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "Error durante el análisis con Interpreter.", e)
            return emptyList()
        }
    }
    private fun uriToBitmap(imageUri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            inputStream.use { stream ->
                if (stream == null) {
                    Log.e("ImageAnalyzer", "No se pudo abrir InputStream para la URI: $imageUri")
                    return null
                }
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                var bitmap = BitmapFactory.decodeStream(stream, null, options)
                if (bitmap != null && bitmap.config != Bitmap.Config.ARGB_8888) {
                    Log.d("ImageAnalyzer", "Convirtiendo bitmap de ${bitmap.config} a ARGB_8888")
                    val convertedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    if (bitmap != convertedBitmap) {
                        bitmap.recycle()
                    }
                    bitmap = convertedBitmap
                }
                if (bitmap == null) {
                    Log.e("ImageAnalyzer", "BitmapFactory.decodeStream devolvió null para la URI: $imageUri")
                } else {
                    Log.d("ImageAnalyzer", "Bitmap cargado con config: ${bitmap.config}")
                }
                return bitmap
            }
        } catch (e: Exception) {
            Log.e("ImageAnalyzer", "Error al convertir URI a Bitmap.", e)
            null
        }
    }
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d("ImageAnalyzer", "Interpreter cerrado.")
    }
}