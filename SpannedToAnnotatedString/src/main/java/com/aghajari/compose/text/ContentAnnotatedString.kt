package com.aghajari.compose.text

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString

/**
 * The data structure of text with multiple styles.
 */
@Immutable
class ContentAnnotatedString(
    val annotatedString: AnnotatedString,
    val inlineContents: List<InlineContent>,
    val paragraphContents: List<ParagraphContent>,
    internal val hasUrl: Boolean =
        annotatedString.hasURL(0, annotatedString.length)
) : CharSequence {

    override val length: Int
        get() = annotatedString.length

    override operator fun get(index: Int): Char = annotatedString[index]

    /**
     * Return a substring for the [ContentAnnotatedString]
     * and include the styles in the range of
     * [startIndex] (inclusive) and [endIndex] (exclusive).
     *
     * @param startIndex the inclusive start offset of the range
     * @param endIndex the exclusive end offset of the range
     */
    override fun subSequence(startIndex: Int, endIndex: Int): ContentAnnotatedString {
        val subAnnotatedString = annotatedString.subSequence(startIndex, endIndex)
        return ContentAnnotatedString(
            annotatedString = subAnnotatedString,
            inlineContents = filterInlineContents(inlineContents, startIndex, endIndex),
            paragraphContents = filterParagraphContents(paragraphContents, startIndex, endIndex),
            hasUrl = hasURL(startIndex, endIndex)
        )
    }

    @Stable
    operator fun plus(other: ContentAnnotatedString): ContentAnnotatedString {
        return ContentAnnotatedString(
            annotatedString = annotatedString + other.annotatedString,
            inlineContents = inlineContents + other.inlineContents,
            paragraphContents = paragraphContents + other.paragraphContents,
            hasUrl = hasUrl or other.hasUrl
        )
    }

    /**
     * Returns URLs attached on this AnnotatedString.
     *
     * @param start the start of the query range, inclusive.
     * @param end the end of the query range, exclusive.
     * @return a list of URLs stored in [AnnotatedString.Range].
     *  Notice that All annotations that intersect with the range
     *  [start, end) will be returned. When [start] is bigger than
     *  [end], an empty list will be returned.
     */
    fun getURLs(start: Int, end: Int): List<AnnotatedString.Range<String>> {
        return annotatedString.getURLs(start, end)
    }

    /**
     * Returns true if [getURLs] with the same parameters
     * would return a non-empty list
     */
    fun hasURL(start: Int, end: Int): Boolean {
        return annotatedString.hasURL(start, end)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentAnnotatedString) return false
        if (hasUrl != other.hasUrl) return false
        if (annotatedString != other.annotatedString) return false
        if (inlineContents != other.inlineContents) return false
        if (paragraphContents != other.paragraphContents) return false
        return true
    }

    override fun hashCode(): Int {
        var result = annotatedString.hashCode()
        result = 31 * result + inlineContents.hashCode()
        result = 31 * result + paragraphContents.hashCode()
        result = 31 * result + hasUrl.hashCode()
        return result
    }

    override fun toString(): String {
        return annotatedString.toString()
    }
}

/**
 * Filter the inline contents to include items only in the range
 * of [start] (inclusive) and [end] (exclusive).
 *
 * @param start the inclusive start offset of the text range
 * @param end the exclusive end offset of the text range
 */
private fun filterInlineContents(
    contents: List<InlineContent>,
    start: Int,
    end: Int
): List<InlineContent> {
    require(start <= end) {
        "start ($start) should be less than or equal to end ($end)"
    }

    return contents.filter { intersect(start, end, it.start, it.end) }.map {
        InlineContent(
            span = it.span,
            id = it.id,
            start = maxOf(start, it.start) - start,
            end = minOf(end, it.end) - start,
            creator = it.creator
        )
    }.toList()
}

/**
 * Filter the paragraph contents to include items only in the range
 * of [start] (inclusive) and [end] (exclusive).
 *
 * @param start the inclusive start offset of the text range
 * @param end the exclusive end offset of the text range
 */
private fun filterParagraphContents(
    contents: List<ParagraphContent>,
    start: Int,
    end: Int
): List<ParagraphContent> {
    require(start <= end) {
        "start ($start) should be less than or equal to end ($end)"
    }

    return contents.filter { intersect(start, end, it.start, it.end) }.map {
        ParagraphContent(
            firstLeadingMargin = it.firstLeadingMargin,
            restLeadingMargin = it.restLeadingMargin,
            start = maxOf(start, it.start) - start,
            end = minOf(end, it.end) - start,
            drawer = it.drawer
        )
    }.toList()
}

/**
 * Helper function that checks if the range [lStart, lEnd) intersects with the range
 * [rStart, rEnd).
 *
 * @return [lStart, lEnd) intersects with range [rStart, rEnd), vice versa.
 */
private fun intersect(lStart: Int, lEnd: Int, rStart: Int, rEnd: Int) =
    maxOf(lStart, rStart) < minOf(lEnd, rEnd) ||
            contains(lStart, lEnd, rStart, rEnd) || contains(rStart, rEnd, lStart, lEnd)

/**
 * Helper function that checks if the range [baseStart, baseEnd) contains the range
 * [targetStart, targetEnd).
 *
 * @return true if [baseStart, baseEnd) contains [targetStart, targetEnd), vice versa.
 * When [baseStart]==[baseEnd] it return true iff [targetStart]==[targetEnd]==[baseStart].
 */
private fun contains(baseStart: Int, baseEnd: Int, targetStart: Int, targetEnd: Int) =
    (baseStart <= targetStart && targetEnd <= baseEnd) &&
            (baseEnd != targetEnd || (targetStart == targetEnd) == (baseStart == baseEnd))