package com.aghajari.compose.test

import android.graphics.drawable.GradientDrawable
import android.text.Html
import android.text.Spanned
import androidx.compose.ui.graphics.Color
import androidx.core.text.HtmlCompat
import com.aghajari.compose.text.BulletSpan
import com.aghajari.compose.text.ContentAnnotatedString
import com.aghajari.compose.text.QuoteSpan
import com.aghajari.compose.text.asAnnotatedString
import com.aghajari.compose.text.replaceSpans
import android.graphics.Color as AndroidColor
import android.text.style.BulletSpan as AndroidBulletSpan
import android.text.style.QuoteSpan as AndroidQuoteSpan

private const val HTML_TEXT = "<h1>Hello World!</h1>" +
        "This is my <a href='https://github.com/Aghajari'>GitHub</a>" +
        "<br>And This is an inline <img src='gd'/> drawable" +
        "<br><b>Bold</b> + <i>Italic</i> = <b><i>BoldItalic</i></b>" +
        "<br><u>Underline</u> + <s>Strikethrough</s> = <u><s>This</s></u>" +
        "<blockquote>This paragraph is a<br>Multi-Line Quote</blockquote>" +
        "<font color='#304FFE'>Which</font> " +
        "<font face='sans-serif-thin'>One</font> " +
        "<font color='#C51162'>Do You</font> " +
        "<span style='background-color:#C5CAE9'>Prefer</span> ?" +
        "<ul>\n" +
        "  <li>Coffee</li>\n" +
        "  <li>Tea</li>\n" +
        "  <li>Milk</li>\n" +
        "</ul>"

private const val FLAGS = HtmlCompat.FROM_HTML_MODE_LEGACY or
        HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST or
        HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM

private val imageGetter = Html.ImageGetter {
    GradientDrawable().apply {
        colors = intArrayOf(AndroidColor.RED, AndroidColor.BLUE)
        cornerRadius = 16f
        setBounds(0, 0, 80, 80)
    }
}

fun getSampleHtml(): ContentAnnotatedString {
    return getAndroidSampleHtml()
        .asAnnotatedString(
            linkColor = Color.Blue
        ).trim() as ContentAnnotatedString
}

fun getAndroidSampleHtml(): Spanned {
    return HtmlCompat.fromHtml(
        HTML_TEXT,
        FLAGS,
        imageGetter,
        null
    ).replaceSpans(
        AndroidQuoteSpan::class.java
    ) {
        QuoteSpan(
            lineColor = AndroidColor.MAGENTA,
            lineStripeWidth = 12,
            paragraphGapWidth = 20,
        )
    }.replaceSpans(
        AndroidBulletSpan::class.java
    ) {
        BulletSpan(
            bulletColor = AndroidColor.MAGENTA,
            radius = 16f,
            paragraphGapWidth = 20,
            strokeWidth = 4f
        )
    }.trim() as Spanned
}