package com.aghajari.compose.text

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.abs

internal val localScaledDensity: Float
    @Composable
    get() = with(LocalDensity.current) { density * fontScale }

internal val scaledDensity: Float
    get() = Resources.getSystem().displayMetrics.scaledDensity

internal val density: Float
    get() = Resources.getSystem().displayMetrics.density

internal fun Int.dpToPx(): Float {
    return this * density
}

internal fun Float.pxToSp(): TextUnit {
    return (this / scaledDensity).sp
}

internal fun Int.pxToSp(): TextUnit {
    return (this / scaledDensity).sp
}

@Composable
internal fun Int.localePxToSp(): TextUnit {
    return (this / localScaledDensity).sp
}

internal fun Drawable.toImageBitmap(): ImageBitmap {
    return toBitmap(
        width = if (minimumWidth > 0) {
            minimumWidth
        } else {
            abs(bounds.width())
        },
        height = if (minimumHeight > 0) {
            minimumHeight
        } else {
            abs(bounds.height())
        }
    ).asImageBitmap()
}
