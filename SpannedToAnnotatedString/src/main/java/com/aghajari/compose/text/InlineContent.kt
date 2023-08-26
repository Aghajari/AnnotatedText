package com.aghajari.compose.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString

/**
 * A Callback to create [InlineTextContent] for the specified span.
 *
 * @see InlineTextContent
 */
fun interface InlineContentCreator {

    @Composable
    fun onCreate(span: Any): InlineTextContent
}

/**
 * A data class that stores [InlineContentCreator]
 * to be inserted into the text layout.
 */
class InlineContent(
    val span: Any,
    val id: String,
    val start: Int,
    val end: Int,
    val creator: InlineContentCreator
)

/**
 * A map store composables that replaces certain ranges of the text.
 * It's used to insert composables into text layout.
 *
 * @see AnnotatedText
 * @see InlineTextContent
 */
@Composable
fun ContentAnnotatedString.getInlineContentMap(): Map<String, InlineTextContent> {
    return if (inlineContents.isEmpty()) {
        emptyMap()
    } else {
        val map = LinkedHashMap<String, InlineTextContent>(inlineContents.size)
        inlineContents.forEach { content ->
            map[content.id] = content.creator.onCreate(content.span)
        }
        if (map.size <= 1) {
            map.toMap()
        } else {
            map
        }
    }
}

/**
 * @see appendInlineContent
 */
internal fun AnnotatedString.Builder.addInlineContent(
    inlineContent: InlineContent
) {
    with(inlineContent) {
        addStringAnnotation(
            tag = INLINE_CONTENT_TAG,
            annotation = id,
            start = start,
            end = end
        )
    }
}

/**
 * The annotation tag used by inline content.
 * @see appendInlineContent
 */
private const val INLINE_CONTENT_TAG = "androidx.compose.foundation.text.inlineContent"