package com.aghajari.compose.text

import android.text.Html
import android.text.style.URLSpan
import androidx.compose.ui.graphics.Color
import androidx.core.text.HtmlCompat

/**
 * Returns displayable styled text from the provided HTML string.
 *
 * @see asAnnotatedString
 * @see Html.fromHtml
 */
fun String.fromHtml(
    flags: Int = HtmlCompat.FROM_HTML_MODE_LEGACY,
    imageGetter: Html.ImageGetter? = null,
    tagHandler: Html.TagHandler? = null,
    spanMappers: SpanMapperMap? = null,
    linkColor: Color = Color.Blue,
    isParagraphContentsEnabled: Boolean = true,
    linkColorMapper: ((URLSpan) -> Color?)? = null
): ContentAnnotatedString {

    return HtmlCompat.fromHtml(
        this,
        flags,
        imageGetter,
        tagHandler,
    ).asAnnotatedString(
        spanMappers,
        linkColor,
        isParagraphContentsEnabled,
        linkColorMapper
    )
}