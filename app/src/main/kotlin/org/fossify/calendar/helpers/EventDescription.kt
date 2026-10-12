package org.fossify.calendar.helpers

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.URLSpan
import java.util.Locale

/** Presentation only: never replace a synchronized event's stored description. */
object EventDescription {
    private val documentTag = Regex("""<\s*(?:html|body)\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val pairedTag = Regex(
        """<\s*(p|div|span|font|a|b|strong|i|em|u|ul|ol|li|blockquote|h[1-6])\b[^>]*>[\s\S]*?</\s*\1\s*>""",
        RegexOption.IGNORE_CASE
    )
    private val lineBreak = Regex("""<br\s*/?>""", RegexOption.IGNORE_CASE)
    private val hiddenContent = Regex(
        """<(head|script|style)\b[^>]*>[\s\S]*?(?:</\1\s*>|$)""",
        RegexOption.IGNORE_CASE
    )

    fun isHtml(description: String): Boolean = documentTag.containsMatchIn(description) ||
        pairedTag.containsMatchIn(description) || lineBreak.containsMatchIn(description)

    fun display(description: String): CharSequence {
        if (!isHtml(description)) return description

        // Native text parsing: no WebView, script execution or remote image loading.
        val parsed = SpannableStringBuilder(
            Html.fromHtml(hiddenContent.replace(description, ""), Html.FROM_HTML_MODE_COMPACT)
        )
        // Use the user's theme and font size rather than Exchange's hard-coded styles.
        parsed.getSpans(0, parsed.length, Any::class.java).forEach { span ->
            when (span) {
                is ForegroundColorSpan, is BackgroundColorSpan,
                is AbsoluteSizeSpan, is RelativeSizeSpan -> parsed.removeSpan(span)
            }
        }
        // Only ordinary web/mail links should be actionable in the preview.
        parsed.getSpans(0, parsed.length, URLSpan::class.java).forEach { span ->
            val scheme = span.url.substringBefore(':', "").lowercase(Locale.ROOT)
            if (scheme !in setOf("https", "http", "mailto")) parsed.removeSpan(span)
        }
        var end = parsed.length
        while (end > 0 && parsed[end - 1] == '\n') end--
        return parsed.subSequence(0, end)
    }

    fun summary(description: String): String = display(description).toString().replace('\n', ' ')
}
