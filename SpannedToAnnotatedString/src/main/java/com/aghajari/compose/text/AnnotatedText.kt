package com.aghajari.compose.text

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/**
 * High level element that displays text and provides semantics / accessibility information.
 *
 * @param text the [ContentAnnotatedString] text to be displayed
 * @param modifier the [Modifier] to be applied to this layout node
 * @param color [Color] to apply to the text.
 * @param fontSize the size of glyphs to use when painting the text. See [TextStyle.fontSize].
 * @param fontStyle the typeface variant to use when drawing the letters (e.g., italic).
 * See [TextStyle.fontStyle].
 * @param fontWeight the typeface thickness to use when painting the text (e.g., [FontWeight.Bold]).
 * @param fontFamily the font family to be used when rendering the text. See [TextStyle.fontFamily].
 * @param letterSpacing the amount of space to add between each letter.
 * See [TextStyle.letterSpacing].
 * @param textDecoration the decorations to paint on the text (e.g., an underline).
 * See [TextStyle.textDecoration].
 * @param textAlign the alignment of the text within the lines of the paragraph.
 * See [TextStyle.textAlign].
 * @param lineHeight line height for the [Paragraph] in [TextUnit] unit, e.g. SP or EM.
 * See [TextStyle.lineHeight].
 * @param overflow how visual overflow should be handled.
 * @param softWrap whether the text should break at soft line breaks. If false, the glyphs in the
 * text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 * [overflow] and TextAlign may have unexpected effects.
 * @param maxLines an optional maximum number of lines for the text to span, wrapping if
 * necessary. If the text exceeds the given number of lines, it will be truncated according to
 * [overflow] and [softWrap]. If it is not null, then it must be greater than zero.
 * @param minLines The minimum height in terms of minimum number of visible lines. It is required
 * that 1 <= [minLines] <= [maxLines].
 * @param inlineContent a map storing composables that replaces certain ranges of the text, used to
 * insert composables into text layout. See [InlineTextContent].
 * @param onTextLayout callback that is executed when a new text layout is calculated. A
 * [TextLayoutResult] object that callback provides contains paragraph information, size of the
 * text, baselines and other details. The callback can be used to add additional decoration or
 * functionality to the text. For example, to draw selection around the text.
 * @param onURLClick callback that is executed when users click
 * the url text. The default implementation
 * Will try to open the url, by launching an an Activity with an
 * [android.content.Intent.ACTION_VIEW] intent. Pass Null to use
 * the default implementation.
 * @param style style configuration for the text such as color, font, line height etc.
 */
@Composable
fun AnnotatedText(
    text: ContentAnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign = TextAlign.Unspecified,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> =
        text.getInlineContentMap(),
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onURLClick: ((String) -> Unit)? = null,
    style: TextStyle = TextStyle.Default
) {
    val textColor = color.takeOrElse {
        style.color.takeOrElse {
            Color.Black
        }
    }

    val mergedStyle = style.merge(
        TextStyle(
            color = textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textAlign = textAlign,
            lineHeight = lineHeight,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            fontStyle = fontStyle,
            letterSpacing = letterSpacing
        )
    )

    BasicAnnotatedText(
        text,
        modifier,
        mergedStyle,
        onTextLayout,
        overflow,
        softWrap,
        maxLines,
        minLines,
        inlineContent,
        onURLClick
    )
}

/**
 * Basic element that displays text and provides semantics / accessibility information.
 *
 * @param text The [ContentAnnotatedString] text to be displayed.
 * @param modifier [Modifier] to apply to this layout node.
 * @param style Style configuration for the text such as color, font, line height etc.
 * @param onTextLayout Callback that is executed when a new text layout is calculated. A
 * [TextLayoutResult] object that callback provides contains paragraph information, size of the
 * text, baselines and other details. The callback can be used to add additional decoration or
 * functionality to the text. For example, to draw selection around the text.
 * @param overflow How visual overflow should be handled.
 * @param softWrap Whether the text should break at soft line breaks. If false, the glyphs in the
 * text will be positioned as if there was unlimited horizontal space. If [softWrap] is false,
 * [overflow] and TextAlign may have unexpected effects.
 * @param maxLines An optional maximum number of lines for the text to span, wrapping if
 * necessary. If the text exceeds the given number of lines, it will be truncated according to
 * [overflow] and [softWrap]. It is required that 1 <= [minLines] <= [maxLines].
 * @param minLines The minimum height in terms of minimum number of visible lines. It is required
 * that 1 <= [minLines] <= [maxLines].
 * @param inlineContent A map store composables that replaces certain ranges of the text. It's
 * used to insert composables into text layout. Check [InlineTextContent] for more information.
 * @param onURLClick callback that is executed when users click
 * the url text. The default implementation
 * Will try to open the url, by launching an an Activity with an
 * [android.content.Intent.ACTION_VIEW] intent. Pass Null to use
 * the default implementation.
 */
@Composable
fun BasicAnnotatedText(
    text: ContentAnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    inlineContent: Map<String, InlineTextContent> =
        text.getInlineContentMap(),
    onURLClick: ((String) -> Unit)? = null
) {
    if (text.hasUrl or text.paragraphContents.isNotEmpty()) {
        ClickableAnnotatedText(
            text = text,
            modifier = modifier,
            style = style,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout,
            onURLClick = onURLClick
        )
    } else {
        BasicText(
            text = text.annotatedString,
            modifier = modifier,
            style = style,
            softWrap = softWrap,
            overflow = overflow,
            maxLines = maxLines,
            minLines = minLines,
            inlineContent = inlineContent,
            onTextLayout = onTextLayout
        )
    }
}

@Composable
private fun ClickableAnnotatedText(
    text: ContentAnnotatedString,
    modifier: Modifier,
    style: TextStyle,
    softWrap: Boolean,
    overflow: TextOverflow,
    maxLines: Int,
    minLines: Int,
    inlineContent: Map<String, InlineTextContent>,
    onTextLayout: (TextLayoutResult) -> Unit,
    onURLClick: ((String) -> Unit)?
) {
    val layoutResult = remember {
        mutableStateOf<TextLayoutResult?>(null)
    }
    var textModifier = modifier

    if (text.hasUrl) {
        textModifier = textModifier.annotatedTextClickable(
            text,
            layoutResult,
            onURLClick
        )
    }

    if (text.paragraphContents.isNotEmpty()) {
        textModifier = textModifier.annotatedTextParagraphContents(
            text,
            layoutResult
        )
    }

    BasicText(
        text = text.annotatedString,
        modifier = textModifier,
        style = style,
        softWrap = softWrap,
        overflow = overflow,
        maxLines = maxLines,
        minLines = minLines,
        inlineContent = inlineContent,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        }
    )
}

/**
 * Handles click event on the URLS
 * in the specified [ContentAnnotatedString].
 *
 * @param text the specified [ContentAnnotatedString]
 * @param layoutResult the text layout result received in onTextLayout
 * @param onURLClick callback that is executed when users click
 *  the url text. The default implementation
 *  Will try to open the url, by launching an an Activity with an
 *  [android.content.Intent.ACTION_VIEW] intent. Pass Null to use
 *  the default implementation.
 */
fun Modifier.annotatedTextClickable(
    text: ContentAnnotatedString,
    layoutResult: MutableState<TextLayoutResult?>,
    onURLClick: ((String) -> Unit)?,
): Modifier = composed {
    val onClick = text.toURLClickable(
        onURLClick = onURLClick ?: defaultOnURLClick(LocalContext.current)
    )
    this.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick(layoutResult.getOffsetForPosition(pos))
            }
        }
    }
}

/**
 * Renders the leading margins for paragraphs that have been styled
 * in the specified [ContentAnnotatedString].
 *
 * @param text the specified [ContentAnnotatedString]
 * @param layoutResult the text layout result received in onTextLayout
 */
fun Modifier.annotatedTextParagraphContents(
    text: ContentAnnotatedString,
    layoutResult: MutableState<TextLayoutResult?>
): Modifier {
    return drawBehind {
        layoutResult.value?.let { layoutResult ->
            text.paragraphContents.forEach { content ->
                val startLine = layoutResult.getLineForOffsetInBounds(
                    offset = content.start,
                    checkIfDelimited = true
                )
                if (startLine < 0) {
                    // Paragraph delimited by maxLines,
                    // ignore the content of the next paragraphs
                    return@drawBehind
                }

                if (content.drawer != null && (content.isDrawerOnly() ||
                            layoutResult.getLineStart(startLine) == content.start)
                ) {
                    val firstEndLine = layoutResult.getLineForOffsetInBounds(
                        offset = content.end - 1
                    )
                    val endOffset = layoutResult.getLineEnd(firstEndLine)
                    val endLine = if (endOffset == content.end) {
                        val nextEndLine = layoutResult.getLineForOffsetInBounds(
                            offset = content.end
                        )
                        if (nextEndLine - firstEndLine > 1) {
                            nextEndLine - 1
                        } else {
                            firstEndLine
                        }
                    } else {
                        firstEndLine
                    }

                    val dir = try {
                        layoutResult.getParagraphDirection(content.start)
                    } catch (ignore: Exception) {
                        return@drawBehind
                    }
                    content.drawer.onDraw(
                        this@drawBehind,
                        ParagraphLayoutInfo(
                            result = layoutResult,
                            startLine = startLine,
                            endLine = endLine,
                            x = if (dir == ResolvedTextDirection.Ltr) {
                                layoutResult.getLineLeft(startLine)
                            } else {
                                layoutResult.getLineRight(startLine)
                            },
                            top = layoutResult.getLineTop(startLine),
                            bottom = layoutResult.getLineBottom(endLine),
                            direction = dir
                        )
                    )
                }
            }
        }
    }
}