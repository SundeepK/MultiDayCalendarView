package sundeepk.github.com.sample;

import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.github.sundeepk.multidaycalendarview.MultiDayCalendarView;
import com.github.sundeepk.multidaycalendarview.domain.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MultiDayCalendarView.MultiDayCalendarListener multiDayCalendarListener;

    private MainActivity activity;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
        multiDayCalendarListener = mock(MultiDayCalendarView.MultiDayCalendarListener.class);

        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);
        multiDayCalendarView.setCalendarListener(multiDayCalendarListener);
    }

    @Test
    public void testItOnNewEventIsCalled(){
        //select first cell in calendar
        ViewAction clickOnCalendar = clickXY(300, 300);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onNewEventCreate(1443744000L);
    }

    @Test
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

    @Test
    public void testItScrollsRight(){
        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(scroll(100, 100, -600, 0));

        //Mon, 05 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onCalendarScroll(any(Date.class));
    }

    @Test
    public void testItScrollsLeft(){
        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(scroll(100, 100, 400, 0));

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onCalendarScroll(any(Date.class));
    }

    @Test
    public void testItSetsHour(){
        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.scrollTo(new Date(1443891600000L));
            }
        });

        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickXY(300, 300));

        //Sat, 03 Oct 2015 17:00:00 GMT
        verify(multiDayCalendarListener).onNewEventCreate(1443891600L);
    }


    @Test
    public void testItRemovesEvent(){
        final MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.addEvent(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));
                assertTrue(multiDayCalendarView.containsEvent(1443744000L));

                multiDayCalendarView.removeEvent(1443744000L);

                assertFalse(multiDayCalendarView.containsEvent(1443744000L));
            }
        });

    }




    public static ViewAction scroll(final int startX, final int startY, final int endX, final int endY){
        return new GeneralSwipeAction(
                Swipe.FAST,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + startX;
                        final float screenY = screenPos[1] + startY;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + endX;
                        final float screenY = screenPos[1] + endY;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
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