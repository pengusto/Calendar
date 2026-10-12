package org.fossify.calendar.helpers

import android.content.Intent
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.fossify.calendar.R
import org.fossify.calendar.activities.EventActivity
import org.fossify.calendar.adapters.EventListWidgetAdapter
import org.fossify.calendar.databases.EventsDatabase
import org.fossify.calendar.extensions.config
import org.fossify.calendar.extensions.eventsHelper
import org.fossify.calendar.models.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDescriptionIntegrationTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val events = EventsDatabase.getInstance(context).EventsDao()
    private val source = "\n<html><head><style>hidden</style></head><body><p>First</p><p>Second</p></body></html>\n"

    private fun withEvent(block: (Long) -> Unit) {
        val now = System.currentTimeMillis() / 1000
        val event = Event(null, startTS = now + 3600, endTS = now + 7200,
            title = "HTML regression", description = source, importId = "html-regression")
        context.eventsHelper.insertEvent(event, addToCalDAV = false, showToasts = false)
        try {
            block(event.id!!)
        } finally {
            context.eventsHelper.deleteEvent(event.id!!, false)
        }
    }

    private fun awaitCondition(condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 10000
        while (!condition() && System.currentTimeMillis() < deadline) Thread.sleep(50)
        assertTrue("Condition did not complete within 10 seconds", condition())
    }

    private fun editAndSave(id: Long, editSource: Boolean) {
        ActivityScenario.launch<EventActivity>(Intent(context, EventActivity::class.java).putExtra(EVENT_ID, id)).use { scenario ->
            awaitCondition {
                var ready = false
                scenario.onActivity { ready = it.findViewById<EditText>(R.id.event_title).text.toString() == "HTML regression" }
                ready
            }
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.event_title).setText("HTML regression saved")
                if (editSource) {
                    activity.findViewById<android.view.View>(R.id.event_description_toggle).performClick()
                    activity.findViewById<EditText>(R.id.event_description).setText("<p>Edited &amp; saved</p>")
                    activity.findViewById<android.view.View>(R.id.event_description_toggle).performClick()
                    assertEquals("Edited & saved", activity.findViewById<TextView>(R.id.event_description_preview).text.toString())
                }
                activity.findViewById<Toolbar>(R.id.event_toolbar).menu.performIdentifierAction(R.id.save, 0)
            }
            awaitCondition { events.getEventWithId(id)?.title == "HTML regression saved" }
        }
    }

    @Test
    fun titleOnlySavePreservesExactHtml() = withEvent { id ->
        editAndSave(id, false)
        assertEquals(source, events.getEventWithId(id)!!.description)
    }

    @Test
    fun explicitSourceEditSavesExactEditedHtml() = withEvent { id ->
        editAndSave(id, true)
        assertEquals("<p>Edited &amp; saved</p>", events.getEventWithId(id)!!.description)
    }

    @Test
    fun widgetRemoteViewShowsReadableSummary() = withEvent {
        val previousDisplay = context.config.displayDescription
        val previousReplace = context.config.replaceDescription
        try {
            context.config.displayDescription = true
            context.config.replaceDescription = false
            val adapter = EventListWidgetAdapter(context, Intent())
            adapter.onDataSetChanged()
            var found = false
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                for (position in 0 until adapter.count) {
                    val view = adapter.getViewAt(position).apply(context, FrameLayout(context))
                    if (view.findViewById<TextView>(R.id.event_item_title)?.text?.toString() == "HTML regression") {
                        val text = view.findViewById<TextView>(R.id.event_item_time).text.toString()
                        assertTrue(text.contains("First Second"))
                        assertTrue(!text.contains("<p>"))
                        found = true
                    }
                }
            }
            assertTrue("Synthetic event missing from widget", found)
        } finally {
            context.config.displayDescription = previousDisplay
            context.config.replaceDescription = previousReplace
        }
    }
}
