package sundeepk.github.com.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.github.sundeepk.multidaycalendarview.MultiDayCalendarView;
import com.github.sundeepk.multidaycalendarview.domain.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) findViewById(R.id.multiday_calendar_view);
        multiDayCalendarView.scrollTo(new Date(1443744000000L));
        multiDayCalendarView.setTimeZoneAndLocale(TimeZone.getTimeZone("GMT"), Locale.getDefault());
        multiDayCalendarView.setCalendarListener(new MultiDayCalendarView.MultiDayCalendarListener() {
            @Override
            public void onNewEventCreate(long eventStartDateTime) {
                multiDayCalendarView.addEvent(eventStartDateTime, new Event<>("Some awesome thiiiiiiiiiinnngfffffffffff event name cool story bro", "some data to hold", Color.parseColor("#43A047")));
            }

            @Override
            public void onEventSelect(long eventStartDateTime, Event selectedEvent) {

            }

            @Override
            public void onCalendarScroll(Date firstDateTimeShownInCalendarView) {

            }
        });

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.getTime();

        cal.add(Calendar.HOUR_OF_DAY, 1);
        multiDayCalendarView.addEvent(cal.getTimeInMillis(), new Event<>("Some awesome thiiiiiiiiiinnngfffffffffff a aa  a a a aa a a a a a a", "some data to hold", Color.parseColor("#43A047")));

        cal.add(Calendar.HOUR_OF_DAY, 1);
        multiDayCalendarView.addEvent(cal.getTimeInMillis(), new Event<>("Some awesome thiiiiiiiiiinnngfffffffffff somethingalsoverylongtotestwith", "some data to hold", Color.parseColor("#43A047")));

        cal.add(Calendar.HOUR_OF_DAY, 1);
        multiDayCalendarView.addEvent(cal.getTimeInMillis(), new Event<>("Some awesome thiiiiiiiiiinnngfffffffffff 11111112222211 11223344334", "some data to hold", Color.parseColor("#43A047")));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
