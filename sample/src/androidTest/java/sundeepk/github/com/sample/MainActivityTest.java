package sundeepk.github.com.sample;

import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.github.sundeepk.multidaycalendarview.MultiDayCalendarView;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static org.mockito.Matchers.anyLong;
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
        multiDayCalendarListener = mock(MultiDayCalendarView.MultiDayCalendarListener.class);
        activity = getActivity();
    }

    @Test
    public void testItOnNewEventIsCalled(){
        ViewAction clickOnCalendar = clickXY(300, 300);
        MultiDayCalendarView multiDayCalendarView = (MultiDayCalendarView) activity.findViewById(R.id.multiday_calendar_view);
        multiDayCalendarView.setCalendarListener(multiDayCalendarListener);
        onView(ViewMatchers.withId(R.id.multiday_calendar_view)).perform(clickOnCalendar);
        verify(multiDayCalendarListener).onNewEventCreate(anyLong());

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