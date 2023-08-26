package com.aghajari.compose.text

import android.os.Build
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign

/**
 * Implementation of an [InlineContentCreator] to
 * insert images into the text layout.
 *
 * @see InlineContentCreator
 * @see ImageSpan
 */
@Immutable
private class ImageInlineContentCreator : InlineContentCreator {

    @Composable
    override fun onCreate(span: Any): InlineTextContent {
        require(span is ImageSpan)

        val imageBitmap = remember(span) {
            span.drawable.toImageBitmap()
        }

        return InlineTextContent(
            placeholder = Placeholder(
                width = imageBitmap.width.localePxToSp(),
                height = imageBitmap.height.localePxToSp(),
                when (span.verticalAlignment) {
                    DynamicDrawableSpan.ALIGN_BOTTOM -> {
                        PlaceholderVerticalAlign.TextBottom
                    }
                    DynamicDrawableSpan.ALIGN_CENTER -> {
                        PlaceholderVerticalAlign.TextCenter
                    }
                    DynamicDrawableSpan.ALIGN_BASELINE -> {
                        PlaceholderVerticalAlign.AboveBaseline
                    }
                    else -> {
                        PlaceholderVerticalAlign.TextTop
                    }
                }
            )
        ) {
            val desc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                span.contentDescription?.toString()
            } else {
                null
            }

            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = desc,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

internal fun ImageSpan.asInlineContent(
    range: IntRange
) = InlineContent(
    span = this,
    id = System.currentTimeMillis().toString() + hashCode(),
    start = range.first,
    end = range.last,
    creator = ImageInlineContentCreator()
)