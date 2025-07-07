package com.example.politicalandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.politicalandroid.navigation.AppNavigation
import com.example.politicalandroid.ui.theme.PoliticalAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoliticalAndroidTheme {
                AppNavigation()
            }
        }
    }
}