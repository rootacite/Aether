package com.acitelight.aether.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiliStyleSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFFFFFFF),
            activeTrackColor = colorScheme.primary,
            inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
        ),

        track = { sliderPositions ->
            Box(
                Modifier
                    .height(trackHeight)
                    .fillMaxWidth()
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .background(colorScheme.primary, RoundedCornerShape(50))
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiliMiniSlider(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    colors: SliderColors = SliderDefaults.colors(
        thumbColor = Color(0xFFFFFFFF),
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = Color.LightGray.copy(alpha = 0.4f)
    )
) {
    val trackHeight = 3.dp

    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = colors,
        enabled = false,
        thumb = {

        },
        track = { sliderPositions ->
            Box(
                Modifier
                    .height(trackHeight)
                    .fillMaxWidth()
                    .background(colors.inactiveTrackColor, RoundedCornerShape(50))
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(value)
                        .fillMaxHeight()
                        .background(colors.activeTrackColor, RoundedCornerShape(50))
                )
            }
        }
    )
}
