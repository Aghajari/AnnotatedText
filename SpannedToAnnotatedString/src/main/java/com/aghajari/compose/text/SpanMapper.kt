package com.aghajari.compose.text

import android.graphics.Typeface
import android.os.Build
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.LocaleSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TextAppearanceSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import kotlin.reflect.KClass

/**
 * Styling configuration for a text span.
 *
 * In the process of merging several Spans in one [SpanStyle],
 * instead of creating several SpanStyles and merging them together,
 * we use [MutableSpanStyle] to consider the values as non-deterministic
 * and after the process is finished, convert it into a [SpanStyle].
 */
@Suppress("MemberVisibilityCanBePrivate")
class MutableSpanStyle internal constructor(
    var linkColor: Color = Color.Unspecified,
    var color: Color = Color.Unspecified,
    var fontSize: TextUnit = TextUnit.Unspecified,
    var fontWeight: FontWeight? = null,
    var fontStyle: FontStyle? = null,
    var fontSynthesis: FontSynthesis? = null,
    var fontFamily: FontFamily? = null,
    var fontFeatureSettings: String? = null,
    var letterSpacing: TextUnit = TextUnit.Unspecified,
    var baselineShift: BaselineShift? = null,
    var background: Color = Color.Unspecified,
    var textDecoration: TextDecoration? = null,
    var textGeometricTransform: TextGeometricTransform? = null,
    var localeList: LocaleList? = null,
    var shadow: Shadow? = null,
    var appearance: SpanStyle? = null,
    var linkColorMapper: ((URLSpan) -> Color?)? = null
) {

    val paragraphContents = mutableListOf<ParagraphContent>()
    internal var urlSpan: URLSpan? = null

    fun toSpanStyle(): SpanStyle {
        return SpanStyle(
            color = if (urlSpan != null && color.isUnspecified) {
                val mappedColor = linkColorMapper?.invoke(requireNotNull(urlSpan))
                if (mappedColor != null && mappedColor.isSpecified) {
                    mappedColor
                } else {
                    linkColor
                }
            } else {
                color
            },
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSynthesis = fontSynthesis,
            fontFamily = fontFamily,
            fontFeatureSettings = fontFeatureSettings,
            letterSpacing = letterSpacing,
            baselineShift = baselineShift,
            background = background,
            textDecoration = textDecoration,
            localeList = localeList,
            shadow = shadow,
            textGeometricTransform = textGeometricTransform
        ).merge(appearance)
    }

    fun toParagraphStyle(): ParagraphStyle? {
        if (hasParagraphStyle().not()) {
            return null
        }

        var first = 0
        var rest = 0
        var lineHeight: Int? = null
        var alignment: TextAlign? = null
        paragraphContents.forEach { paragraph ->
            first = paragraph.firstLeadingMargin ?: first
            rest = paragraph.restLeadingMargin ?: rest
            lineHeight = paragraph.lineHeight ?: lineHeight
            alignment = paragraph.alignment ?: alignment
        }

        val indent = if (first != 0 || rest != 0) {
            TextIndent(firstLine = first.pxToSp(), restLine = rest.pxToSp())
        } else null

        return ParagraphStyle(
            textAlign = alignment,
            textIndent = indent,
            lineHeight = lineHeight?.pxToSp() ?: TextUnit.Unspecified
        )
    }

    fun hasParagraphStyle() = paragraphContents.isNotEmpty()
}

internal typealias SpanMapperMap = Map<KClass<*>, SpanMapper<*>?>
internal typealias SpanMapper<T> = MutableSpanStyle.(span: T) -> Unit
internal typealias PairSpanMapper<T> = Pair<KClass<T>, SpanMapper<T>>

/**
 * @return a map of all default span mapper functions.
 */
internal fun getDefaultSpanMappers(): MutableMap<KClass<*>, SpanMapper<*>?> {
    return mutableMapOf(
        absoluteSize(),
        relativeSize(),
        backgroundColor(),
        foregroundColor(),
        strikethrough(),
        underline(),
        style(),
        subscript(),
        superscript(),
        scaleX(),
        skewX(),
        locales(),
        textAppearance(),
        typeface(),
        urlStyle()
    )
}

private fun absoluteSize(): PairSpanMapper<AbsoluteSizeSpan> {
    return AbsoluteSizeSpan::class to {
        val sizeInPx = if (it.dip) {
            it.size.dpToPx()
        } else {
            it.size.toFloat()
        }
        fontSize = sizeInPx.pxToSp()
    }
}

private fun relativeSize(): PairSpanMapper<RelativeSizeSpan> {
    return RelativeSizeSpan::class to {
        fontSize = it.sizeChange.em
    }
}

private fun backgroundColor(): PairSpanMapper<BackgroundColorSpan> {
    return BackgroundColorSpan::class to {
        background = Color(it.backgroundColor)
    }
}

private fun foregroundColor(): PairSpanMapper<ForegroundColorSpan> {
    return ForegroundColorSpan::class to {
        color = Color(it.foregroundColor)
    }
}

private fun strikethrough(): PairSpanMapper<StrikethroughSpan> {
    return StrikethroughSpan::class to {
        textDecoration += TextDecoration.LineThrough
    }
}

private fun underline(): PairSpanMapper<UnderlineSpan> {
    return UnderlineSpan::class to {
        textDecoration += TextDecoration.Underline
    }
}

private fun style(): PairSpanMapper<StyleSpan> {
    return StyleSpan::class to {
        when (it.style) {
            Typeface.ITALIC,
            Typeface.BOLD_ITALIC -> fontStyle = FontStyle.Italic
            Typeface.NORMAL -> fontStyle = FontStyle.Normal
        }
        fontWeight = fontWeight.adjust(it)
    }
}

private fun subscript(): PairSpanMapper<SubscriptSpan> {
    return SubscriptSpan::class to {
        baselineShift = BaselineShift.Subscript
    }
}

private fun superscript(): PairSpanMapper<SuperscriptSpan> {
    return SuperscriptSpan::class to {
        baselineShift = BaselineShift.Superscript
    }
}

private fun scaleX(): PairSpanMapper<ScaleXSpan> {
    return ScaleXSpan::class to {
        textGeometricTransform = if (textGeometricTransform != null) {
            textGeometricTransform?.copy(
                scaleX = textGeometricTransform!!.scaleX * it.scaleX
            )
        } else {
            TextGeometricTransform(scaleX = it.scaleX)
        }
    }
}

private fun skewX(): PairSpanMapper<SkewXSpan> {
    return SkewXSpan::class to {
        textGeometricTransform = if (textGeometricTransform != null) {
            textGeometricTransform?.copy(
                skewX = textGeometricTransform!!.skewX + it.skewX
            )
        } else {
            TextGeometricTransform(skewX = it.skewX)
        }
    }
}

private fun locales(): PairSpanMapper<LocaleSpan> {
    return LocaleSpan::class to {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            localeList = LocaleList(it.locales.toLanguageTags())
        } else if (it.locale != null) {
            localeList = LocaleList(Locale(it.locale!!.toLanguageTag()))
        }
    }
}

private fun textAppearance(): PairSpanMapper<TextAppearanceSpan> {
    return TextAppearanceSpan::class to {
        appearance = if (appearance != null) {
            requireNotNull(appearance)
                .merge(it.toSpanStyle())
        } else {
            it.toSpanStyle()
        }
        if (it.linkTextColor != null) {
            linkColor = Color(it.linkTextColor.defaultColor)
        }
    }
}

private fun typeface(): PairSpanMapper<TypefaceSpan> {
    return TypefaceSpan::class to {
        fontFamily = it.asFontFamily()
    }
}

private fun urlStyle(): PairSpanMapper<URLSpan> {
    return URLSpan::class to {
        textDecoration += TextDecoration.Underline
    }
}

private operator fun TextDecoration?.plus(decoration: TextDecoration): TextDecoration {
    return (this ?: TextDecoration.None) + decoration
}

@Suppress("UNCHECKED_CAST")
internal operator fun <T : Any> SpanMapperMap.get(
    span: T
): SpanMapper<T>? {
    if (containsKey(span::class)) {
        return get(span::class) as? SpanMapper<T>
    }
    return firstOrNull {
        it.value != null && it.key.isInstance(span)
    } as? SpanMapper<T>
}

private inline fun <K, V> Map<out K, V>.firstOrNull(
    predicate: (Map.Entry<K, V>) -> Boolean
): V? {
    for (element in this) {
        if (predicate(element)) {
            return element.value
        }
    }
    return null
}