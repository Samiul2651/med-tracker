package dev.samiul.medtracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/** Step 1 placeholder. Real screens arrive in steps 4–5 of plan.md. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text("Med Tracker", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

@Preview
@Composable
private fun AppPreview() = App()
