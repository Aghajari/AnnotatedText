package com.aghajari.compose.text

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.text.style.IconMarginSpan as AndroidIconMarginSpan
import android.text.style.DrawableMarginSpan as AndroidDrawableMarginSpan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.ResolvedTextDirection

/**
 * Implementation of an [ParagraphContentDrawer] to
 * render an icon at start of the paragraph.
 */
private class IconParagraphContentDrawer(
    val image: ImageBitmap?
) : ParagraphContentDrawer {

    override fun onDraw(
        drawScope: DrawScope,
        layoutInfo: ParagraphLayoutInfo
    ) {
        if (image == null) return

        drawScope.drawImage(
            image = image,
            topLeft = Offset(
                x = if (layoutInfo.direction == ResolvedTextDirection.Rtl) {
                    layoutInfo.x - image.width
                } else {
                    layoutInfo.x
                },
                y = layoutInfo.top
            )
        )
    }
}

internal fun AndroidIconMarginSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    return asParagraphContent(
        range = range,
        drawer = IconParagraphContentDrawer(
            image = getImage()
        )
    )
}

internal fun AndroidDrawableMarginSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    return asParagraphContent(
        range = range,
        drawer = IconParagraphContentDrawer(
            image = getImage()
        )
    )
}

@SuppressLint("PrivateApi")
private fun AndroidIconMarginSpan.getImage(): ImageBitmap? {
    return try {
        if (this is IconMarginSpan) {
            bitmap.asImageBitmap()
        } else {
            javaClass.getDeclaredField("mBitmap")
                .run {
                    isAccessible = true
                    return (get(this@getImage) as? Bitmap)
                        ?.asImageBitmap()
                }
        }
    } catch (ignore: Exception) {
        null
    }
}

@SuppressLint("PrivateApi")
private fun AndroidDrawableMarginSpan.getImage(): ImageBitmap? {
    return try {
        if (this is DrawableMarginSpan) {
            drawable.toImageBitmap()
        } else {
            javaClass.getDeclaredField("mDrawable")
                .run {
                    isAccessible = true
                    return (get(this@getImage) as? Drawable)
                        ?.toImageBitmap()
                }
        }
    } catch (ignore: Exception) {
        null
    }
}

class IconMarginSpan(
    val bitmap: Bitmap,
    padding: Int = 0
) : AndroidIconMarginSpan(bitmap, padding)

class DrawableMarginSpan(
    val drawable: Drawable,
    padding: Int = 0
) : AndroidDrawableMarginSpan(drawable, padding)