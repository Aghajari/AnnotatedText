package com.aghajari.compose.text

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.Layout
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import android.text.style.QuoteSpan as AndroidQuoteSpan

/**
 * Implementation of [ParagraphContentDrawer] to
 * render the quote vertical stripe.
 *
 * @see ParagraphContentDrawer
 * @see QuoteSpan
 */
private class QuoteParagraphContentDrawer(
    val color: Color,
    val stripeWidth: Int
) : ParagraphContentDrawer {

    override fun onDraw(
        drawScope: DrawScope,
        layoutInfo: ParagraphLayoutInfo
    ) {
        drawScope.drawRect(
            color = color,
            topLeft = Offset(layoutInfo.x, layoutInfo.top),
            size = Size(
                width = layoutInfo.dirSign * stripeWidth.toFloat(),
                height = layoutInfo.height
            )
        )
    }
}

internal fun AndroidQuoteSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    return if (this is QuoteSpan) {
        asParagraphContent(
            range = range,
            drawer = QuoteParagraphContentDrawer(
                color = Color(lineColor),
                stripeWidth = lineStripeWidth,
            )
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        asParagraphContent(
            range = range,
            drawer = QuoteParagraphContentDrawer(
                color = Color(color),
                stripeWidth = stripeWidth,
            )
        )
    } else {
        asParagraphContent(
            range = range,
            drawer = QuoteParagraphContentDrawer(
                color = Color(color),
                stripeWidth = 2,
            )
        )
    }
}

/**
 * A span which styles paragraphs by adding a vertical stripe
 * at the beginning of the text (respecting layout direction).
 *
 * @see AndroidQuoteSpan
 */
class QuoteSpan(
    val lineColor: Int = -0xffff01,
    val lineStripeWidth: Int = 2,
    private val paragraphGapWidth: Int = 2
) : AndroidQuoteSpan() {

    private constructor(parcel: Parcel) : this(
        lineColor = parcel.readInt(),
        lineStripeWidth = parcel.readInt(),
        paragraphGapWidth = parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(lineColor)
        dest.writeInt(lineStripeWidth)
        dest.writeInt(paragraphGapWidth)
    }

    override fun getColor() = lineColor
    override fun getStripeWidth() = lineStripeWidth
    override fun getGapWidth() = paragraphGapWidth

    override fun getLeadingMargin(first: Boolean): Int {
        return lineStripeWidth + paragraphGapWidth
    }

    override fun drawLeadingMargin(
        c: Canvas, p: Paint, x: Int, dir: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        first: Boolean, layout: Layout
    ) {
        val style = p.style
        val color = p.color
        p.style = Paint.Style.FILL
        p.color = lineColor
        c.drawRect(
            x.toFloat(),
            top.toFloat(),
            (x + dir * lineStripeWidth).toFloat(),
            bottom.toFloat(),
            p
        )
        p.style = style
        p.color = color
    }

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR = object : Parcelable.Creator<QuoteSpan> {
            override fun createFromParcel(parcel: Parcel) = QuoteSpan(parcel)
            override fun newArray(size: Int) = arrayOfNulls<QuoteSpan>(size)
        }
    }
}