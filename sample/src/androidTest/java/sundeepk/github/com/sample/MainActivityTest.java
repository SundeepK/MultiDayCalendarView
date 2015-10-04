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
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.github.sundeepk.multidaycalendarview.MultiDayCalendarView;
import com.github.sundeepk.multidaycalendarview.domain.Event;

import org.junit.Before;

import java.util.Date;

import static android.support.test.espresso.Espresso.onView;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private MultiDayCalendarView.MultiDayCalendarListener multiDayCalendarListener;
    private MultiDayCalendarView multiDayCalendarView;

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
        multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);
        multiDayCalendarView.setCalendarListener(multiDayCalendarListener);
    }

    public void testOnNewEventIsCalled(){
        //select first cell in calendar
        ViewAction clickOnCalendar = clickXY(300, 300);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onNewEventCreate(1443744000L);
    }

    public void testItSelectsEvent(){
        givenCalendarHasEvent(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));

        //select first cell in calendar
        ViewAction clickOnCalendar = clickXY(300, 300);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onEventSelect(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));
    }

    public void testItScrollsRight(){
        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(scroll(100, 100, -600, 0));

        //Mon, 05 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onCalendarScroll(any(Date.class));
    }

    public void testItScrollsLeft(){
        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(scroll(100, 100, 400, 0));

        //Fri, 02 Oct 2015 00:00:00 GMT
        verify(multiDayCalendarListener).onCalendarScroll(any(Date.class));
    }

    public void testItSetsHour(){
        givenCalendarIsScrolledTo(new Date(1443891600000L));

        //select first cell in calendar
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickXY(300, 300));

        //Sat, 03 Oct 2015 17:00:00 GMT
        verify(multiDayCalendarListener).onNewEventCreate(1443891600L);
    }


    public void testItSetsDay(){
        givenCalendarIsScrolledTo(new Date(1444492800000L));

        //Sat, 10 Oct 2015 00:00:00 GMT
        assertEquals(new Date(1444435200000L), multiDayCalendarView.getCurrentLeftMostDay());
    }

    public void testItSetsDayInThePast(){
        givenCalendarIsScrolledTo(new Date(1410111870000L));

        //Sun, 07 Sep 2014 00:00:00 GMT
        assertEquals(new Date(1410048000000L), multiDayCalendarView.getCurrentLeftMostDay());
    }

    public void testItRemovesEvent(){
        givenCalendarHasEvent(1443744000L, new Event<>("Some event", null, Color.parseColor("#43A047")));

        assertTrue(multiDayCalendarView.containsEvent(1443744000L));

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.removeEvent(1443744000L);
            }
        });

        assertFalse(multiDayCalendarView.containsEvent(1443744000L));
    }

    private void givenCalendarIsScrolledTo(final Date date) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.scrollTo(date);
            }
        });
    }

    private void givenCalendarHasEvent(final long epochTime, final Event<?> objectEvent) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                multiDayCalendarView.addEvent(epochTime, objectEvent);
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
