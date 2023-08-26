package com.aghajari.compose.text

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import androidx.compose.ui.text.font.AndroidFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.toFontFamily
import kotlin.math.max
import kotlin.math.min

/**
 * @return [FontFamily] of the given [TypefaceSpan].
 */
internal fun TypefaceSpan.asFontFamily(): FontFamily? {
    return if (family != null) {
        getFontFamily(family)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getFontFamily(typeface)
    } else {
        null
    }
}

/**
 * @return [FontFamily] of the given family name.
 */
internal fun getFontFamily(family: String?): FontFamily? {
    return when (family?.lowercase()) {
        FontFamily.SansSerif.name -> FontFamily.SansSerif
        FontFamily.Serif.name -> FontFamily.Serif
        FontFamily.Monospace.name -> FontFamily.Monospace
        FontFamily.Cursive.name -> FontFamily.Cursive
        else -> if (family != null) {
            Typeface.create(family, Typeface.NORMAL)
                .toFontFamily()
        } else null
    }
}

/**
 * @return [FontFamily] of the given [Typeface].
 */
internal fun getFontFamily(typeface: Typeface?): FontFamily? {
    return when (typeface) {
        Typeface.SANS_SERIF -> FontFamily.SansSerif
        Typeface.SERIF -> FontFamily.Serif
        Typeface.MONOSPACE -> FontFamily.Monospace
        else -> typeface?.toFontFamily()
    }
}

private class TypefaceAsFont(
    typeface: Typeface
) : AndroidFont(
    FontLoadingStrategy.OptionalLocal,
    ReadyTypefaceLoader(typeface),
    FontVariation.Settings()
) {

    override val style: FontStyle =
        if (typeface.isItalic) {
            FontStyle.Italic
        } else {
            FontStyle.Normal
        }

    override val weight: FontWeight =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            FontWeight(typeface.weight)
        } else if (typeface.isBold) {
            FontWeight.Bold
        } else {
            FontWeight.Normal
        }
}

private class ReadyTypefaceLoader(
    val typeface: Typeface
) : AndroidFont.TypefaceLoader {

    override suspend fun awaitLoad(
        context: Context,
        font: AndroidFont
    ): Typeface {
        return typeface
    }

    override fun loadBlocking(
        context: Context,
        font: AndroidFont
    ): Typeface {
        return typeface
    }
}

private fun Typeface.toFontFamily(): FontFamily {
    return TypefaceAsFont(this).toFontFamily()
}

/**
 * @return new [FontWeight] adjusted with the given [StyleSpan].
 */
internal fun FontWeight?.adjust(styleSpan: StyleSpan): FontWeight? {
    val fw = when (styleSpan.style) {
        Typeface.BOLD,
        Typeface.BOLD_ITALIC -> FontWeight.Bold
        Typeface.NORMAL -> FontWeight.Normal
        else -> this
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        && isAcceptableWeight(styleSpan.fontWeightAdjustment)
    ) {
        val def = fw?.weight ?: FontWeight.Normal.weight
        return FontWeight(
            max(
                min(
                    def + styleSpan.fontWeightAdjustment,
                    MAX_FONT_WEIGHT
                ),
                MIN_FONT_WEIGHT
            )
        )
    }
    return fw
}

/**
 * @return True if weight is in range of [1, 1000]
 */
internal fun isAcceptableWeight(weight: Int): Boolean {
    return weight in MIN_FONT_WEIGHT..MAX_FONT_WEIGHT
}

private const val MIN_FONT_WEIGHT = 1
private const val MAX_FONT_WEIGHT = 1000