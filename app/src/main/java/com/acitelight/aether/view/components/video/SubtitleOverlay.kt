package com.acitelight.aether.view.components.video

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.text.Cue

@Composable
fun SubtitleOverlay(
    cues: List<Cue>,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    textSize: TextUnit = 14.sp,
    backgroundAlpha: Float = 0.6f,
    horizontalMargin: Dp = 16.dp,
    bottomMargin: Dp = 14.dp,
    contentPadding: Dp = 6.dp,
    cornerRadius: Dp = 6.dp,
    textColor: Color = Color.White
) {
    val raw = if (cues.isEmpty()) "" else cues.joinToString(separator = "\n") {
        it.text?.toString() ?: ""
    }.trim()
    if (raw.isEmpty()) return

    val textAlign = when (cues.firstOrNull()?.textAlignment) {
        Layout.Alignment.ALIGN_CENTER -> TextAlign.Center
        Layout.Alignment.ALIGN_OPPOSITE -> TextAlign.End
        Layout.Alignment.ALIGN_NORMAL -> TextAlign.Start
        else -> TextAlign.Center
    }

    val blurPx = with(LocalDensity.current) { (2.dp).toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(start = horizontalMargin, end = horizontalMargin, bottom = bottomMargin)
                .wrapContentWidth(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color.Black.copy(alpha = backgroundAlpha))
                .padding(horizontal = 12.dp, vertical = contentPadding)
        ) {
            Text(
                text = raw,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = textColor,
                    fontSize = textSize,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = blurPx
                    )
                ),
                textAlign = textAlign,
                modifier = Modifier
            )
        }
    }
}