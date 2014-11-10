package cogs198.timeline;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.view.View;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.activity_main);

        int eventCalendarID = 0;
        int eventNameIndex = 1;
        int eventDescriptionIndex = 2;
        int eventStartIndex = 3;
        int eventEndIndex = 4;
        int eventLocationIndex = 5;
        int eventRecurrenceRuleIndex = 6;
        int eventRecurrenceDateIndex = 7;
        Event head = null;
        Timeline timeline = new Timeline(this);

        String name, description;
        long start = 0;
        long end;
        int priority;

        String[] projection = new String[] { "calendar_id", "title", "description",
                "dtstart", "dtend", "eventLocation", "rrule", "rdate"};


        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(Uri.parse("content://com.android.calendar/events"),
                projection,
                null,
                null,
                null);

        while (cursor.moveToNext()) {

            name = cursor.getString(eventNameIndex);
            start = cursor.getLong(eventStartIndex);
            end = cursor.getLong(eventEndIndex);
            priority = cursor.getInt(eventDescriptionIndex);

            if (head == null)
                head = new Event(name, start, end, priority, null, null);
            else
                head = head.addEvent(name, start, end, priority, head);
        }

        timeline.setHead(head);
        setContentView(timeline);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
