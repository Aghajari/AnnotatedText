package com.aghajari.compose.text

import android.text.style.LeadingMarginSpan
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextIndent

/**
 * A Callback to render the leading margin.
 */
fun interface ParagraphContentDrawer {

    fun onDraw(
        drawScope: DrawScope,
        layoutInfo: ParagraphLayoutInfo
    )
}

/**
 * A paragraph style affecting the leading margin.
 *
 * ParagraphContents should be attached from the first
 * character to the last character of a single paragraph.
 */
class ParagraphContent(
    val firstLeadingMargin: Int,
    val restLeadingMargin: Int,
    val start: Int,
    val end: Int,
    val drawer: ParagraphContentDrawer
)

/**
 * The data class which holds paragraph area and
 * text layout result.
 */
@Suppress("unused")
class ParagraphLayoutInfo(
    val result: TextLayoutResult,
    val startLine: Int,
    val endLine: Int,
    val x: Float,
    val top: Float,
    val bottom: Float,
    val direction: ResolvedTextDirection
) {

    val height: Float
        get() = bottom - top

    internal val dirSign: Int
        get() = if (direction == ResolvedTextDirection.Ltr) {
            +1
        } else {
            -1
        }
}

internal fun AnnotatedString.Builder.addParagraphContent(
    paragraphContent: ParagraphContent
) {
    with(paragraphContent) {
        val first = firstLeadingMargin.pxToSp()
        val rest = restLeadingMargin.pxToSp()
        if (first.value != 0f || rest.value != 0f) {
            addStyle(
                style = ParagraphStyle(
                    textIndent = TextIndent(
                        firstLine = first,
                        restLine = rest
                    )
                ),
                start = start,
                end = end
            )
        }
    }
}

internal fun LeadingMarginSpan.asParagraphContent(
    range: IntRange,
    drawer: ParagraphContentDrawer
): ParagraphContent {
    return ParagraphContent(
        firstLeadingMargin = getLeadingMargin(true),
        restLeadingMargin = getLeadingMargin(false),
        start = range.first,
        end = range.last,
        drawer = drawer
    )
}

internal fun LeadingMarginSpan.Standard.asParagraphContent(
    range: IntRange
): ParagraphContent {
    return asParagraphContent(
        range = range,
        drawer = { _, _ -> }
    )
}