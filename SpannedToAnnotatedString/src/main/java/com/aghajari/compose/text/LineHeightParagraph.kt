package com.aghajari.compose.text

import android.graphics.Paint
import android.text.style.LineHeightSpan

internal fun LineHeightSpan.asParagraphContent(
    range: IntRange
): ParagraphContent {
    val fm = Paint.FontMetricsInt().apply {
        ascent = 0
        descent = 1
    }
    chooseHeight("", 0, 0, 0, 0, fm)

    return ParagraphContent(
        start = range.first,
        end = range.last,
        lineHeight = fm.descent
    )
}