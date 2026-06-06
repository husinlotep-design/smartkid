package com.smartkids.launcher.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas


fun Modifier.grayscaleDetox(active: Boolean): Modifier =
    this.drawWithContent {

        drawContent()


        if (active) {
            drawIntoCanvas { canvas ->
                // ColorMatrix with saturation 0 removes all colour
                val paint = Paint().apply {
                    colorFilter = ColorFilter.colorMatrix(
                        ColorMatrix().apply { setToSaturation(0f) }
                    )
                }
                // Cover the entire composable area with the grey paint
                canvas.drawRect(
                    left   = 0f,
                    top    = 0f,
                    right  = size.width,
                    bottom = size.height,
                    paint  = paint
                )
            }
        }
    }