package com.aghajari.compose.text

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Browser
import android.text.style.URLSpan
import android.util.Log
import androidx.compose.ui.text.AnnotatedString

/**
 * Callback that is executed when users click the url text.
 */
internal fun ContentAnnotatedString.toURLClickable(
    onURLClick: (String) -> Unit
): (Int) -> Unit {
    return { offset ->
        getURLs(offset, offset)
            .firstOrNull()?.let {
                onURLClick(it.item)
            }
    }
}

/**
 * Default Implementation of URL click callback.
 * Will try to open the url, by launching an an Activity with an
 * [android.content.Intent.ACTION_VIEW] intent.
 *
 * @see toURLClickable
 * @see URLSpan.onClick
 */
internal fun defaultOnURLClick(
    context: Context
): (String) -> Unit {
    return {
        Intent(Intent.ACTION_VIEW, Uri.parse(it)).apply {
            putExtra(Browser.EXTRA_APPLICATION_ID, context.packageName)
            try {
                context.startActivity(this)
            } catch (e: ActivityNotFoundException) {
                Log.w(
                    "AnnotatedText",
                    "Activity was not found for intent, $this"
                )
            }
        }
    }
}

// TODO: Use [AnnotatedString.Builder.addUrlAnnotation]
//  whenever it exits the experimental mode
/**
 * Marks the given range as url.
 */
internal fun AnnotatedString.Builder.addURL(
    urlSpan: URLSpan,
    range: IntRange
): Boolean {
    addStringAnnotation(
        tag = URL_TAG,
        annotation = urlSpan.url,
        start = range.first,
        end = range.last
    )
    return true
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
internal fun AnnotatedString.getURLs(
    start: Int,
    end: Int
): List<AnnotatedString.Range<String>> {
    return getStringAnnotations(URL_TAG, start, end)
}

/**
 * Returns true if [getURLs] with the same parameters
 * would return a non-empty list
 */
internal fun AnnotatedString.hasURL(
    start: Int,
    end: Int
): Boolean {
    return hasStringAnnotations(URL_TAG, start, end)
}

/**
 * The annotation tag used to distinguish urls.
 */
private const val URL_TAG = "com.aghajari.compose.text.urlAnnotation"