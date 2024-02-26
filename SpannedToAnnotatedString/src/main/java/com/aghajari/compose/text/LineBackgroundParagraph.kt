package com.aghajari.compose.text

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.ParcelableSpan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import android.text.style.LineBackgroundSpan as AndroidLineBackgroundSpan

/**
 * Implementation of [ParagraphContentDrawer] to
 * render the background color of the lines to which the span is attached.
 *
 * @see ParagraphContentDrawer
 * @see LineBackgroundSpan
 */
private class LineBackgroundContentDrawer(
    val color: Color,
) : ParagraphContentDrawer {

    override fun onDraw(
        drawScope: DrawScope,
        layoutInfo: ParagraphLayoutInfo
    ) {
        drawScope.drawRect(
            color = color,
            topLeft = Offset(0f, layoutInfo.top),
            size = Size(
                width = layoutInfo.result.size.width.toFloat(),
                height = layoutInfo.height
            )
        )
    }
}

internal fun AndroidLineBackgroundSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    val color = if (this is LineBackgroundSpan) {
        Color(this.color)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        this is AndroidLineBackgroundSpan.Standard
    ) {
        Color(this.color)
    } else {
        Color.Unspecified
    }

    return ParagraphContent(
        start = range.first,
        end = range.last,
        drawer = if (color.isSpecified) {
            LineBackgroundContentDrawer(color)
        } else null
    )
}

/**
 * @see AndroidLineBackgroundSpan
 */
class LineBackgroundSpan(
    val color: Int = -0xffff01,
) : AndroidLineBackgroundSpan, ParcelableSpan {

    private constructor(parcel: Parcel) : this(
        color = parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(color)
    }

    override fun describeContents(): Int = 0
    override fun getSpanTypeId(): Int = 27

    override fun drawBackground(
        canvas: Canvas, paint: Paint,
        left: Int, right: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        lineNumber: Int
    ) {
        val originColor = paint.color
        paint.color = color
        canvas.drawRect(
            left.toFloat(),
            top.toFloat(),
            right.toFloat(),
            bottom.toFloat(),
            paint
        )
        paint.color = originColor
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<LineBackgroundSpan> {
            override fun createFromParcel(parcel: Parcel) = LineBackgroundSpan(parcel)
            override fun newArray(size: Int) = arrayOfNulls<LineBackgroundSpan>(size)
        }
    }
}