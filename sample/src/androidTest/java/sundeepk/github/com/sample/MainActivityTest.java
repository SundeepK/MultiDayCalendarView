package sundeepk.github.com.sample;

import android.graphics.Color;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.github.sundeepk.multidaycalendarview.MultiDayCalendarView;
import com.github.sundeepk.multidaycalendarview.domain.Event;

import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MultiDayCalendarView.MultiDayCalendarListener multiDayCalendarListener;

    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
        multiDayCalendarListener = mock(MultiDayCalendarView.MultiDayCalendarListener.class);

        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);
        multiDayCalendarView.setCalendarListener(multiDayCalendarListener);
    }

    public void testItOnNewEventIsCalled(){
        //select first cell in calendar
        ViewAction clickOnCalendar = clickXY(300, 300);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onNewEventCreate(1443744000L);
    }

    public void testItSelectsEvent(){
        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.addEvent(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));
            }
        });

        //select first cell in calendar
        ViewAction clickOnCalendar = clickXY(300, 300);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onEventSelect(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));
    }



    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }



}