package com.aghajari.compose.text

import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.text.style.TextAppearanceSpan
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Converts [TextAppearanceSpan] to a [SpanStyle].
 */
internal fun TextAppearanceSpan.toSpanStyle(): SpanStyle {
    var mColor: Color = Color.Unspecified
    var mFontSize: TextUnit = TextUnit.Unspecified
    var mFontWeight: FontWeight? = null
    var mFontStyle: FontStyle? = null
    var mFontFamily: FontFamily? = null
    var mLocaleList: LocaleList? = null
    var mFontFeatureSettings: String? = null
    var mShadow: Shadow? = null

    if (family.isNullOrEmpty().not()) {
        mFontFamily = getFontFamily(family)
    }

    when (textStyle) {
        Typeface.ITALIC -> mFontStyle = FontStyle.Italic
        Typeface.NORMAL -> mFontStyle = FontStyle.Normal
        Typeface.BOLD -> mFontWeight = FontWeight.Bold
        Typeface.BOLD_ITALIC -> {
            mFontStyle = FontStyle.Italic
            mFontWeight = FontWeight.Bold
        }
    }

    if (textSize != -1) {
        val displayMetrics = Resources.getSystem().displayMetrics
        mFontSize = (textSize / displayMetrics.scaledDensity).sp
    }

    if (textColor != null) {
        mColor = Color(textColor.defaultColor)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mFontFeatureSettings = fontFeatureSettings

        if (typeface != null) {
            mFontFamily = getFontFamily(typeface)
        }

        if (isAcceptableWeight(textFontWeight)) {
            mFontWeight = FontWeight(textFontWeight)
        }

        if (textLocales != null) {
            mLocaleList = LocaleList(requireNotNull(textLocales).toLanguageTags())
        }

        if (shadowColor != 0) {
            mShadow = Shadow(
                color = Color(shadowColor),
                offset = Offset(shadowDx, shadowDy),
                blurRadius = shadowRadius
            )
        }
    }

    return SpanStyle(
        color = mColor,
        fontSize = mFontSize,
        fontWeight = mFontWeight,
        fontStyle = mFontStyle,
        fontFamily = mFontFamily,
        fontFeatureSettings = mFontFeatureSettings,
        localeList = mLocaleList,
        shadow = mShadow
    )
}