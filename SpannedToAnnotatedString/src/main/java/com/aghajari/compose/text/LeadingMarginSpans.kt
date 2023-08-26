package com.aghajari.compose.text

import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.IconMarginSpan
import android.text.style.DrawableMarginSpan
import android.text.style.LeadingMarginSpan
import android.text.style.QuoteSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import java.lang.IllegalArgumentException

/**
 * Adding [ParagraphStyle] to [AnnotatedString] causes the specified
 * range to be separated completely. As a result, the scope of
 * a paragraph becomes completely independent. The beginning of
 * the range will be the beginning of the paragraph and the end of
 * the range will be the end of the paragraph. Therefore,
 * in order not to display additional NewLines, we must first
 * rearrange the text by considering how ParagraphStyle works
 * and identify and remove the additional NewLines.
 *
 * For example this spanned:
 * ```
 *  buildSpannedString {
 *     append("Hello\n")
 *     inSpans(BulletSpan()) {
 *         append("Item\n")
 *     }
 *     append("Done")
 *  }
 * ```
 * Will result in:
 * ```
 * Hello
 *
 * - Item
 *
 * Done
 * ```
 * This function will identify and remove the additional NewLines,
 * And the final result must be:
 * ```
 * Hello
 * - Item
 * Done
 * ```
 */
internal fun Spanned.supportLeadingMarginSpans(): Spanned {
    val spans = getSpans(0, length, LeadingMarginSpan::class.java)
    spans.sortBy { getSpanStart(it) }

    return if (spans.isNotEmpty()) {
        toBuilder().apply {
            var reserved = -1
            spans.forEach { span ->
                setSpan(
                    span,
                    getSpanStart(span),
                    getSpanEnd(span),
                    PARAGRAPH_CONTENT
                )
            }
            spans.forEach { span ->
                var start = getSpanStart(span)
                if (start < reserved) {
                    removeSpan(span)
                } else {
                    var remove = when {
                        span.isSupportedLeadingMarginSpan().not() -> -1
                        start == 0 -> 0
                        start == reserved -> 0
                        get(start - 1) == NEW_LINE && start == reserved + 1 -> {
                            replace(start - 1, start, CRLF)
                            start++
                            1
                        }
                        get(start - 1) == NEW_LINE -> 1
                        else -> -1
                    }

                    var end = getSpanEnd(span)
                    if (getOrNull(end - 1) != NEW_LINE) {
                        subSequence(end, length).indexOfFirst {
                            it == NEW_LINE
                        }.let { indexOfNext ->
                            end = if (indexOfNext == -1) {
                                length
                            } else {
                                end + indexOfNext + 1
                            }
                            setSpan(span, start, end, PARAGRAPH_CONTENT)
                        }
                    }

                    if (remove == -1) {
                        if (span.supportsInternalBreakLine()) {
                            removeSpan(span)
                            val nextNewLine = subSequence(start, end - 1).indexOfFirst {
                                it == NEW_LINE
                            }
                            if (nextNewLine != -1) {
                                setSpan(
                                    span,
                                    nextNewLine + start + 1,
                                    end,
                                    PARAGRAPH_CONTENT
                                )
                                replace(start + nextNewLine, start + nextNewLine + 1, EMPTY)
                                remove = 0
                            }
                        } else {
                            removeSpan(span)
                        }
                    } else if (remove > 0) {
                        replace(start - remove, start, EMPTY)
                        remove = 0
                    }

                    if (remove == 0) {
                        end = getSpanEnd(span)

                        if (getOrNull(end - 1) == NEW_LINE) {
                            if (length != end) {
                                replace(end - 1, end, EMPTY)
                                end--
                            }
                        }
                        reserved = end
                    }
                }
            }
        }
    } else {
        this
    }
}

private const val PARAGRAPH_CONTENT = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
private const val NEW_LINE = '\n'
private const val CRLF = "\r\n"
private const val EMPTY = ""

internal fun LeadingMarginSpan.isSupportedLeadingMarginSpan(): Boolean {
    return this is QuoteSpan ||
            this is BulletSpan ||
            this is IconMarginSpan ||
            this is DrawableMarginSpan ||
            this is LeadingMarginSpan.Standard
}

internal fun toParagraphContent(
    span: Any,
    range: IntRange
): ParagraphContent {
    return when (span) {
        is BulletSpan -> span.asParagraphContent(range)
        is QuoteSpan -> span.asParagraphContent(range)
        is IconMarginSpan -> span.asParagraphContent(range)
        is DrawableMarginSpan -> span.asParagraphContent(range)
        is LeadingMarginSpan.Standard -> span.asParagraphContent(range)
        else -> throw IllegalArgumentException(
            "${span.javaClass.name} is not supported!"
        )
    }
}

private fun LeadingMarginSpan.supportsInternalBreakLine(): Boolean {
    return isSupportedLeadingMarginSpan() &&
            this !is BulletSpan
}