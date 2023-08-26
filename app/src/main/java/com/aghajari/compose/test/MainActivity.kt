package com.aghajari.compose.test

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aghajari.compose.test.ui.theme.SpannedToAnnotatedStringTheme
import com.aghajari.compose.text.AnnotatedText
import android.graphics.Color as AndroidColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpannedToAnnotatedStringTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 16.dp
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    SampleAnnotatedText()
                    Divider(Modifier.padding(vertical = 16.dp))
                    SampleAndroidText()
                    Divider(Modifier.padding(vertical = 16.dp))
                }
            }
        }
    }
}

@Composable
fun SampleAnnotatedText(modifier: Modifier = Modifier) {
    AnnotatedText(
        text = getSampleHtml(),
        color = Color.Black,
        fontSize = 16.sp,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun SampleAndroidText(modifier: Modifier = Modifier) {
    AndroidView(
        factory = {
            TextView(it).apply {
                text = getAndroidSampleHtml()
                setTextColor(AndroidColor.BLACK)
                setLinkTextColor(AndroidColor.BLUE)
                textSize = 16f
                movementMethod = LinkMovementMethod()
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    SpannedToAnnotatedStringTheme {
        SampleAnnotatedText()
    }
}