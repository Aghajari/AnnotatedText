package com.aghajari.compose.text

import android.text.SpannableStringBuilder
import android.text.Spanned

/**
 * @return an [SpannableStringBuilder] containing the specified text.
 *  Returns a copy if the specified text isn't a builder itself.
 */
internal fun Spanned.toBuilder(): SpannableStringBuilder {
    return if (this is SpannableStringBuilder) {
        this
    } else {
        SpannableStringBuilder(this)
    }
}

/**
 * Replace all the specified kind spans in the given range with
 * a new span.
 */
fun Spanned.replaceSpans(
    kind: Class<*>,
    queryStart: Int = 0,
    queryEnd: Int = length,
    replacement: (Any) -> Any
): Spanned {
    return toBuilder().apply {
        getSpans(queryStart, queryEnd, kind).forEach { span ->
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            val flags = getSpanFlags(span)

            removeSpan(span)
            setSpan(replacement(span), start, end, flags)
        }
    }
}