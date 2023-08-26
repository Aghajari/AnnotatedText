package com.aghajari.compose.text

import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.LeadingMarginSpan
import android.text.style.URLSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/**
 * Create an [ContentAnnotatedString] from the given [Spanned].
 *
 * @param spanMappers the list of mappers to map an span to [SpanStyle].
 *  The given span mappers will be replaced with default mappers,
 *  Pass Null to use all default span mappers.
 *  If you set a specific kind of Span equal to Null,
 *  all Spans of that kind will be ignored.
 * @param linkColor the default color for URLs.
 * @param isParagraphContentsEnabled Specifies whether to use [ParagraphContent]s
 *  such as [QuoteSpan] and [BulletSpan] or not.
 *  For correct display, you must use [AnnotatedText]
 *  or add [annotatedTextParagraphContents] to modifiers.
 *  @author AmirHossein Aghajari
 */
fun Spanned.asAnnotatedString(
    spanMappers: SpanMapperMap? = null,
    linkColor: Color = Color.Blue,
    isParagraphContentsEnabled: Boolean = true
): ContentAnnotatedString {
    val fixed = if (isParagraphContentsEnabled) {
        supportLeadingMarginSpans()
    } else {
        this
    }

    val mappers = getDefaultSpanMappers()
    if (spanMappers != null) {
        mappers.putAll(spanMappers)
    }

    var hasUrl = false
    val inlineContent = mutableListOf<InlineContent>()
    val paragraphContent = mutableListOf<ParagraphContent>()

    val annotatedString = buildAnnotatedString {
        append(fixed.toString())

        fixed.mapSpans().forEach { (range, spans) ->
            mergeSpans(
                spans = spans,
                range = range,
                linkColor = linkColor,
                urlSpanMapper = { urlSpan ->
                    hasUrl = true
                    addURL(urlSpan, range)
                },
                inlineContentMapper = { content ->
                    inlineContent.add(content)
                    addInlineContent(content)
                },
                paragraphContentMapper = if (isParagraphContentsEnabled) {
                    { content ->
                        paragraphContent.add(content)
                        addParagraphContent(content)
                    }
                } else null,
                mappers
            ).let {
                addStyle(it, range.first, range.last)
            }
        }
    }

    return ContentAnnotatedString(
        annotatedString = annotatedString,
        inlineContents = inlineContent.optimize(),
        paragraphContents = paragraphContent.optimize(),
        hasUrl = hasUrl
    )
}

/**
 * Merges all spans in the given range and
 * returns a combined [SpanStyle].
 *
 * This helps to use only one [SpanStyle] for all customizations
 * instead of adding multiple SpanStyle for a fixed range
 * to the AnnotatedString.
 */
private fun mergeSpans(
    spans: List<Any>,
    range: IntRange,
    linkColor: Color,
    urlSpanMapper: (URLSpan) -> Unit,
    inlineContentMapper: (InlineContent) -> Unit,
    paragraphContentMapper: ((ParagraphContent) -> Unit)?,
    spanMapper: SpanMapperMap
): SpanStyle {
    val style = MutableSpanStyle(linkColor)

    spans.forEach { span ->
        when (span) {
            is ImageSpan ->
                inlineContentMapper(span.asInlineContent(range))
            is URLSpan -> {
                style.isUrl = true
                urlSpanMapper(span)
            }
            is LeadingMarginSpan -> {
                if (paragraphContentMapper != null &&
                    span.isSupportedLeadingMarginSpan()
                ) {
                    paragraphContentMapper(toParagraphContent(span, range))
                }
            }
        }

        val mapper = spanMapper[span]
        mapper?.invoke(style, span)
    }

    return style.toSpanStyle()
}

/**
 * Maps all spans to their range and sorts based on start range.
 */
private fun Spanned.mapSpans(): Map<IntRange, List<Any>> {
    val spansMap = mutableMapOf<IntRange, MutableList<Any>>()

    getSpans(0, length, Any::class.java).forEach { span ->
        val range = IntRange(getSpanStart(span), getSpanEnd(span))
        spansMap.getOrPut(range) {
            mutableListOf()
        }.add(span)
    }

    return spansMap.toSortedMap { o1, o2 ->
        o1.first.compareTo(o2.first)
    }
}

/**
 * Returns a [List] containing all elements.
 */
private fun <T> MutableList<T>.optimize(): List<T> {
    return if (size <= 1) {
        toList()
    } else {
        this
    }
}