package com.rain.hiltondemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.rain.hiltondemo.ui.navigation.NavGraph
import com.rain.hiltondemo.ui.theme.HiltonDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiltonDemoTheme {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
