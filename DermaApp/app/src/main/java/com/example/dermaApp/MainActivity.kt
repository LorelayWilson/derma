package com.example.dermaApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dermaApp.navigation.DermaAppNavigator // Asumiendo que mueves el navegador
import com.example.dermaApp.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DermaAppNavigator() // O tu pantalla principal si no usas un navegador complejo
            }
        }
    }
}