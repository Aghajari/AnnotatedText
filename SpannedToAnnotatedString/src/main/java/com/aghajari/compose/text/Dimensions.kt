package com.aghajari.compose.text

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.TypedValueCompat
import kotlin.math.abs
import kotlin.math.min

internal val localScaledDensity: Float
    @Composable
    get() = with(LocalDensity.current) { density * fontScale }

internal val density: Float
    get() = Resources.getSystem().displayMetrics.density

internal fun Int.dpToPx(): Float {
    return this * density
}

internal fun Float.pxToSp(): TextUnit {
    return try {
        TypedValueCompat.pxToSp(
            this,
            Resources.getSystem().displayMetrics
        ).sp
    } catch (_: Throwable) {
        // This might happen on compose previews
        @Suppress("DEPRECATION")
        (this / Resources.getSystem().displayMetrics.scaledDensity).sp
    }
}

internal fun Int.pxToSp(): TextUnit {
    return toFloat().pxToSp()
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

/**
 * Returns the line number on which the specified text offset appears within
 * a TextLayoutResult, considering the visibility constraints imposed by maxLines.
 *
 * @param offset a character offset
 * @param checkIfDelimited indicates whether to check if the offset is within a line delimiter.
 * @return the line number associated with the specified offset, adjusted for maxLines constraints.
 *         If checkIfDelimited is true and the offset is not within a line delimiter, -1 is returned.
 */
internal fun TextLayoutResult.getLineForOffsetInBounds(
    offset: Int,
    checkIfDelimited: Boolean = false
): Int {
    return try {
        min(getLineForOffset(offset), lineCount - 1)
    } catch (ignore: Exception) {
        if (checkIfDelimited) {
            -1
        } else {
            lineCount - 1
        }
    }
}