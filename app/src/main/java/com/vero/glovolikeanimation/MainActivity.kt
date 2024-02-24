package com.vero.glovolikeanimation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import com.vero.glovolikeanimation.ui.theme.GlovoLikeAnimationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultPath = PathParser().parsePathString(getString(R.string.default_path))
            .toPath()

        setContent {
            GlovoLikeAnimationTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Black
                ) {
                    GlovoLikeAnimation(
                        onGoalClick = { item ->
                            Log.d("Glovo Item", item.title)
                        },
                        mainItem = GlovoItem("Main", defaultPath),
                        items = listOf(
                            GlovoItem("Secondary 1", defaultPath),
                            GlovoItem("Secondary 2", defaultPath),
                            GlovoItem("Secondary 3", defaultPath),
                            GlovoItem("Secondary 4", defaultPath),
                            GlovoItem("Secondary 5", defaultPath),
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GlovoLikeAnimationTheme {
        Greeting("Android")
    }
}