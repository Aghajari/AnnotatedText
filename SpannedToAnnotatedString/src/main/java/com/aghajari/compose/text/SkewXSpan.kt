package com.aghajari.compose.text

import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.compose.ui.text.style.TextGeometricTransform

/**
 * The shear of the text on the horizontal direction.
 * A pixel at (x, y), where y is the distance above baseline,
 * will be transformed to (x + y * skewX, y).
 *
 * @see TextPaint.setTextSkewX
 * @see TextGeometricTransform.skewX
 */
class SkewXSpan(
    val skewX: Float
) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        ds.textSkewX += skewX
    }

    override fun updateMeasureState(ds: TextPaint) {
        ds.textSkewX += skewX
    }
}