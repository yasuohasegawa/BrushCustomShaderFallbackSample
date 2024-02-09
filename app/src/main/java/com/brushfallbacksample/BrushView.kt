package com.brushfallbacksample

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun BrushViewCompose(){
    var time by remember { mutableStateOf(0f) }
    val CUSTOM_SHADER = ShaderUtils.FRAGMENT_SHADER_CODE_BRUSH

    LaunchedEffect(Unit) {
        var lastFrameTime = System.nanoTime()
        while (true) {
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f // Convert nanoseconds to seconds
            lastFrameTime = currentTime
            time += deltaTime

            delay(16) // Aiming for around 60fps
        }
    }

    // onDisappear
    DisposableEffect(Unit) {
        // Code inside here will be executed when the composable is first displayed (onAppear)
        println(">>>> DisposableEffect appear")
        onDispose {
            // Code inside here will be executed when the composable leaves the composition (onDisappear)
            println(">>>> BrushViewCompose disappear")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(1f,1f,1f,1f)
    ) {
        Box(
            // ZStack
            modifier = Modifier.fillMaxSize(),
        ) {
            // Android 13 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithCache {
                            val shader = RuntimeShader(CUSTOM_SHADER)
                            val shaderBrush = ShaderBrush(shader)
                            shader.setFloatUniform("resolution", size.width, size.height)
                            onDrawBehind {
                                shader.setColorUniform(
                                    "color",
                                    android.graphics.Color.valueOf(
                                        ShaderUtils.color[0],
                                        ShaderUtils.color[1],
                                        ShaderUtils.color[2],
                                        ShaderUtils.color[3]
                                    )
                                )
                                shader.setColorUniform(
                                    "color2",
                                    android.graphics.Color.valueOf(
                                        ShaderUtils.color2[0],
                                        ShaderUtils.color2[1],
                                        ShaderUtils.color2[2],
                                        ShaderUtils.color2[3]
                                    )
                                )
                                shader.setFloatUniform("iTime", time)
                                drawRect(shaderBrush)
                            }
                        }
                )
            } else {
                // fallback
                GLShaderCompose()
            }

        }
    }
}
