package com.aghajari.compose.text

import android.text.Spanned
import android.text.style.ImageSpan
import android.text.style.ParagraphStyle as AndroidParagraphStyle
import android.text.style.URLSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import kotlin.math.max
import kotlin.math.min

/**
 * Create a [ContentAnnotatedString] from the given [Spanned].
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
    isParagraphContentsEnabled: Boolean = true,
    linkColorMapper: ((URLSpan) -> Color?)? = null
): ContentAnnotatedString {
    val fixed = if (isParagraphContentsEnabled) {
        supportParagraphStyleSpans()
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

        val paragraphStyles = mutableListOf<ParagraphStyleHolder>()
        fixed.mapSpans().forEach { (range, spans) ->
            mergeSpans(
                spans = spans,
                range = range,
                linkColor = linkColor,
                linkColorMapper = linkColorMapper,
                urlSpanMapper = { urlSpan ->
                    hasUrl = true
                    addURL(urlSpan, range)
                },
                inlineContentMapper = { content ->
                    inlineContent.add(content)
                    addInlineContent(content)
                },
                isParagraphContentsEnabled,
                mappers
            ).let {
                addStyle(it.toSpanStyle(), range.first, range.last)
                if (it.hasParagraphStyle()) {
                    paragraphStyles.safeAdd(ParagraphStyleHolder(it, range))
                }
            }
        }

        paragraphStyles.forEach {
            paragraphContent.addAll(it.style.paragraphContents)
            addStyle(requireNotNull(it.style.toParagraphStyle()), it.range.first, it.range.last)
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
 * Merges all spans within the specified range and returns a combined [MutableSpanStyle].
 *
 * This function aids in consolidating multiple spans applied to the same text range
 * into a single [SpanStyle], promoting a more concise and maintainable approach to
 * text styling within the [ContentAnnotatedString.annotatedString].
 */
private fun mergeSpans(
    spans: List<Any>,
    range: IntRange,
    linkColor: Color,
    linkColorMapper: ((URLSpan) -> Color?)? = null,
    urlSpanMapper: (URLSpan) -> Unit,
    inlineContentMapper: (InlineContent) -> Unit,
    supportsParagraphContent: Boolean,
    spanMapper: SpanMapperMap
): MutableSpanStyle {
    val style = MutableSpanStyle(
        linkColor = linkColor,
        linkColorMapper = linkColorMapper
    )

    spans.forEach { span ->
        when (span) {
            is ImageSpan ->
                inlineContentMapper(span.asInlineContent(range))
            is URLSpan -> {
                style.urlSpan = span
                urlSpanMapper(span)
            }
            is AndroidParagraphStyle -> {
                if (supportsParagraphContent &&
                    span.isSupportedParagraphStyle()
                ) {
                    val content = toParagraphContent(span, range)
                    style.paragraphContents.add(content)
                }
            }
        }

        val mapper = spanMapper[span]
        mapper?.invoke(style, span)
    }

    return style
}

/**
 * Maps each span to its corresponding text range and sorts the mappings based on the starting
 * range. This function facilitates organizing spans applied to text by their respective
 * ranges, allowing for efficient processing and manipulation of text styling. By mapping spans to
 * their ranges and merging multiple spans within the same range into a single [SpanStyle], we can
 * simplify the text styling logic and improve efficiency. Creating only one [SpanStyle] for
 * multiple spans in the same range reduces redundancy and ensures consistent styling for overlapping
 * text spans.
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
        if (o1.first == o2.first) {
            o1.last.compareTo(o2.last)
        } else {
            o1.first.compareTo(o2.first)
        }
    }
}

/**
 * Optimizes the list by returning a new list with the same elements if the size of the original list
 * is less than or equal to 1. Otherwise, it returns the original list itself. This optimization helps
 * reduce memory consumption by avoiding the need for a mutable list when there are 0 or 1 elements,
 * which are immutable by nature. By returning an immutable list in such cases, unnecessary memory
 * overhead associated with maintaining mutability is avoided.
 */
private fun <T> MutableList<T>.optimize(): List<T> {
    return if (size <= 1) {
        toList()
    } else {
        this
    }
}

/**
 * A data class to hold paragraph styles along with their respective ranges within a text.
 */
private data class ParagraphStyleHolder(
    val style: MutableSpanStyle,
    var range: IntRange
)

/**
 * Safely adds a new paragraph style holder to the list, merging it with any existing holder
 * if there is an overlap between their ranges. Overlapping styles can lead to inconsistencies
 * and unexpected rendering behavior. By merging overlapping styles, we ensure that each range
 * of text has a unique set of paragraph styles applied, preventing redundancy and conflicts
 *
 * @param newStyle The new paragraph style holder to add or merge.
 */
private fun MutableList<ParagraphStyleHolder>.safeAdd(newStyle: ParagraphStyleHolder) {
    for (old in this) {
        if (old.range.overlap(newStyle.range)) {
            old.style.paragraphContents.addAll(newStyle.style.paragraphContents)
            old.range = IntRange(
                min(old.range.first, newStyle.range.first),
                max(old.range.last, newStyle.range.last),
            )
            return
        }
    }
    add(newStyle)
}

private fun IntRange.overlap(other: IntRange): Boolean {
    val max = maxOf(first, last)
    val min = minOf(first, last)
    return (other.first in min..<max) || (other.last in min..<max)
}