package com.aghajari.compose.text

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.Layout
import android.text.Spanned
import android.text.style.BulletSpan as AndroidBulletSpan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.isUnspecified
import kotlin.math.ceil

/**
 * Implementation of [ParagraphContentDrawer] to
 * render the paragraph bullet points.
 *
 * @see ParagraphContentDrawer
 * @see BulletSpan
 */
private class BulletParagraphContentDrawer(
    val color: Color,
    val bulletRadius: Float,
    val strokeWidth: Float = Float.NaN
) : ParagraphContentDrawer {

    override fun onDraw(
        drawScope: DrawScope,
        layoutInfo: ParagraphLayoutInfo
    ) {
        val startBottom = layoutInfo.result.getLineBottom(layoutInfo.startLine)
        val (radius, style) = if (strokeWidth.isNaN()) {
            bulletRadius to Fill
        } else {
            (bulletRadius - strokeWidth / 2f) to Stroke(strokeWidth)
        }

        drawScope.drawCircle(
            color = if (color.isUnspecified) {
                layoutInfo.result.layoutInput.style.color
            } else {
                color
            },
            center = Offset(
                x = layoutInfo.x + layoutInfo.dirSign * bulletRadius,
                y = (layoutInfo.top + startBottom) / 2f
            ),
            radius = radius,
            style = style
        )
    }
}

internal fun AndroidBulletSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    return if (this is BulletSpan) {
        asParagraphContent(
            range = range,
            drawer = BulletParagraphContentDrawer(
                color = if (wantColor) {
                    Color(bulletColor)
                } else {
                    Color.Unspecified
                },
                bulletRadius = radius,
                strokeWidth = strokeWidth
            )
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        asParagraphContent(
            range = range,
            drawer = BulletParagraphContentDrawer(
                color = if (color != 0) {
                    Color(color)
                } else {
                    Color.Unspecified
                },
                bulletRadius = bulletRadius.toFloat(),
            )
        )
    } else {
        asParagraphContent(
            range = range,
            drawer = BulletParagraphContentDrawer(
                color = Color.Unspecified,
                bulletRadius = 4f,
            )
        )
    }
}

/**
 * A span which styles paragraphs as bullet points
 * (respecting layout direction)
 *
 * This span supports Stroke draw style mode.
 *
 * @see AndroidBulletSpan
 */
class BulletSpan(
    val bulletColor: Int = 0,
    val radius: Float = 4f,
    val strokeWidth: Float = Float.NaN,
    private val paragraphGapWidth: Int = 2
) : AndroidBulletSpan() {

    internal val wantColor: Boolean
        get() = bulletColor != 0

    private constructor(parcel: Parcel) : this(
        bulletColor = parcel.readInt(),
        radius = parcel.readFloat(),
        strokeWidth = parcel.readFloat(),
        paragraphGapWidth = parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(bulletColor)
        dest.writeFloat(radius)
        dest.writeFloat(strokeWidth)
        dest.writeInt(paragraphGapWidth)
    }

    override fun getGapWidth() = paragraphGapWidth
    override fun getBulletRadius() = radius.toInt()
    override fun getColor() = bulletColor

    override fun getLeadingMargin(first: Boolean): Int {
        return ceil(2 * radius + paragraphGapWidth).toInt()
    }

    override fun drawLeadingMargin(
        canvas: Canvas, paint: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout?
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            var stroke = 0f
            var oldColor = 0
            val circleRadius: Float
            if (wantColor) {
                oldColor = paint.color
                paint.color = bulletColor
            }
            if (strokeWidth.isNaN()) {
                paint.style = Paint.Style.FILL
                circleRadius = radius
            } else {
                stroke = paint.strokeWidth
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                circleRadius = radius - (strokeWidth / 2f)
            }
            canvas.drawCircle(
                x + dir * radius,
                (top + bottom) / 2f,
                circleRadius,
                paint
            )
            if (wantColor) {
                paint.color = oldColor
            }
            if (strokeWidth.isNaN().not()) {
                paint.strokeWidth = stroke
            }
            paint.style = style
        }
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<BulletSpan> {
            override fun createFromParcel(parcel: Parcel) = BulletSpan(parcel)
            override fun newArray(size: Int) = arrayOfNulls<BulletSpan>(size)
        }
    }
}