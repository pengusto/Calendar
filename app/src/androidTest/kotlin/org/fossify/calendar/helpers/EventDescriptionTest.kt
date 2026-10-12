package org.fossify.calendar.helpers

import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDescriptionTest {
    @Test
    fun exchangeDocumentDisplaysOnlyItsBody() {
        val source = """<html><head><meta name="Generator" content="Microsoft Exchange Server">
            <style>body { color: red; }</style></head><body><font size="2"><span style="font-size:11pt;">
            <div class="PlainText">Workshop confirmed.</div></span></font></body></html>"""
        assertEquals("Workshop confirmed.", EventDescription.display(source).toString().trim())
    }

    @Test
    fun plainTextAndLiteralAngleBracketsAreUnchanged() {
        val source = "Meet at 10:00\nx < 5 and y > 2; bring <notes>"
        assertFalse(EventDescription.isHtml(source))
        assertEquals(source, EventDescription.display(source))
    }

    @Test
    fun paragraphsEntitiesAndLinksRemainReadable() {
        val preview = EventDescription.display("<p>First &amp; second</p><p>Next<br>line <a href=\"https://example.com\">Details</a></p>") as Spanned
        assertTrue(preview.toString().contains("First & second\nNext\nline Details"))
        assertEquals("https://example.com", preview.getSpans(0, preview.length, URLSpan::class.java).single().url)
    }

    @Test
    fun scriptsStylesAndUnsafeLinksAreNotPresented() {
        val preview = EventDescription.display("<html><body><script>alert('bad')</script><style>hidden</style><p><a href=\"javascript:bad()\">Label</a></p></body></html>") as Spanned
        assertEquals("Label", preview.toString())
        assertTrue(preview.getSpans(0, preview.length, URLSpan::class.java).isEmpty())
    }

    @Test
    fun summariesDoNotExposeMarkup() {
        assertEquals("First Second", EventDescription.summary("<div>First</div><div>Second</div>"))
    }

    @Test
    fun formattingDoesNotOverrideThemeOrFontSize() {
        val preview = EventDescription.display("<font color=\"#000000\" size=\"1\">Readable</font>") as Spanned
        assertEquals("Readable", preview.toString())
        assertTrue(preview.getSpans(0, preview.length, ForegroundColorSpan::class.java).isEmpty())
        assertTrue(preview.getSpans(0, preview.length, AbsoluteSizeSpan::class.java).isEmpty())
    }

    @Test
    fun malformedDocumentAndListsRemainReadable() {
        val preview = EventDescription.display("<html><body><ul><li>First</li><li>Second</li></ul><p>Last")
        assertTrue(preview.toString().contains("First"))
        assertTrue(preview.toString().contains("Second"))
        assertTrue(preview.toString().contains("Last"))
        assertFalse(preview.toString().contains("<li>"))
    }
}
