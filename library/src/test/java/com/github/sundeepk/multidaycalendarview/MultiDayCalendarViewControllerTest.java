package com.github.sundeepk.multidaycalendarview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.widget.OverScroller;

import com.github.sundeepk.multidaycalendarview.domain.Event;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultiDayCalendarViewControllerTest {

    private static final float HEADER_HEIGHT = 200;
    private static final float TIME_COLUMN_PADDING = 80;
    private static final float HEADER_TEST_TOP_PADDING = 60;
    private static final int NUMBER_DAYS_VISIBLE = 3;
    private static final int PARENT_HEIGHT = 800;
    private static final int PARENT_WIDTH = 600;
    private static final float TEXT_HEIGHT = 20;
    private static final int TIME_TEXT_HEIGHT = 20 * 6;
    private PointF accumulatedScrollOffset;
    private float[] dayLines = new float[(NUMBER_DAYS_VISIBLE) * 4 * 3];
    private float[] headerDayLines = new float[(NUMBER_DAYS_VISIBLE) * 4 * 3];
    private float[] hourLines = new float[24 * 4];

    @Mock private Paint dayHourSeparatorPaint;
    @Mock private Paint timeColumnPaint;
    @Mock private TextPaint eventsPaint;
    @Mock private OverScroller overScroller;
    @Mock private Canvas canvas;
    @Mock private MotionEvent event;
    @Mock private RectF measureTextSizeRect;
    @Mock private Rect textRect;
    @Mock private MotionEvent motionEvent;
    @Mock private GestureDetectorCompat gestureDetectorCompat;
    @Mock private MultiDayCalendarView.MultiDayCalendarListener listener;

    private MultiDayCalendarViewController underTest;
    private int timeTextWidth = 0;
    private int padding = 10;
    private int headerColumnWidth = timeTextWidth + padding * 2;
    private int widthPerDay = PARENT_WIDTH - headerColumnWidth - padding * (NUMBER_DAYS_VISIBLE);

    @Before
    public void setUp(){
        when(measureTextSizeRect.height()).thenReturn(TEXT_HEIGHT);
        widthPerDay = widthPerDay / NUMBER_DAYS_VISIBLE;
        accumulatedScrollOffset = new PointF(0f, 0f);
        underTest = new MultiDayCalendarViewController(dayHourSeparatorPaint, timeColumnPaint, eventsPaint, overScroller, measureTextSizeRect, textRect);
    }


    @Test
    public void itDrawsDayHeaderAndTimeColumn() {
        Calendar currentCalendar =  Calendar.getInstance();
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        List<Date> dates = new ArrayList<>();
        List<String> hoursWithAmPm = new ArrayList<>();

        for(int day = -NUMBER_DAYS_VISIBLE; day < (NUMBER_DAYS_VISIBLE * 2); day++){
            currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
            currentCalendar.set(Calendar.MINUTE, 0);
            currentCalendar.set(Calendar.SECOND, 0);
            currentCalendar.set(Calendar.MILLISECOND, 0);
            currentCalendar.add(Calendar.DATE, 1);
            dates.add(currentCalendar.getTime());
        }

        for (int hour = 1, day = -NUMBER_DAYS_VISIBLE; hour < 24; hour++) {
            if (hour > 11) {
                hoursWithAmPm.add(Integer.toString(hour) + "pm");
            } else {
                hoursWithAmPm.add(Integer.toString(hour) + "am");
            }
        }

        givenDayAndHourLines(0, widthPerDay);
        underTest.onMeasure(PARENT_WIDTH, PARENT_HEIGHT);
        underTest.onDraw(canvas);

        for (int hour = 0, day = 0; hour < 23; hour++, day++) {
            if (day < dates.size() ) {
                verify(canvas, atMost(1)).drawText(eq(Integer.toString(getDayOfMonth(dates.get(day)))), anyInt(), anyInt(), eq(timeColumnPaint));
                verify(canvas, atMost(2)).drawText(eq(getDayOfWeek(dates.get(day))), anyInt(), anyInt(), eq(timeColumnPaint));
            }
            verify(canvas).drawText(eq(hoursWithAmPm.get(hour)), anyInt(), anyInt(), eq(timeColumnPaint));
        }
    }

    @Test
    public void testItAddsEvent(){
        Event<String> event = new Event<>("Some test event", "Test data passed in", 100, true);
        underTest.addEvent(1, event);
        assertEquals(event, underTest.removeEvent(1));
    }

    @Test
    public void testListenerIsCalledOnMonthScroll(){
        //Sun, 01 Mar 2015 00:00:00 GMT
        Date expectedDateOnScroll = new Date(1423440000000L);

        when(motionEvent.getAction()).thenReturn(MotionEvent.ACTION_UP);

        //Set width of view so that scrolling will return a correct value
        underTest.onMeasure(720, 1080);

        //Sun, 08 Feb 2015 00:00:00 GMT
        underTest.gotoDate(new Date(1423353600000L));

        underTest.setMultiDayCalendarListener(listener);

        //Scroll enough to push calendar to next month
        underTest.onScroll(motionEvent, motionEvent, 200, 0);
        underTest.onDraw(canvas);
        underTest.onTouchEvent(motionEvent, gestureDetectorCompat);
        assertEquals(expectedDateOnScroll, underTest.getCurrentLeftMostDay());
        verify(listener).onCalendarScroll(new Date(1423440000000L));
    }


    private int getDayOfMonth(Date aDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    private String getDayOfWeek(Date aDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(aDate);
        return cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
    }

    private void givenDayAndHourLines(int daysScrolledSoFar, int widthPerDay) {
        float dayXPos;
        for (int step = 0, i = 0, day = -NUMBER_DAYS_VISIBLE -(daysScrolledSoFar); step < 24; step++) {
            float top = accumulatedScrollOffset.y + TIME_TEXT_HEIGHT * step + (HEADER_HEIGHT);
            int i1 = i * 4;
            int i2 = i1 + 1;
            int i3 = i1 + 2;
            int i4 = i1 + 3;
            if (day < (NUMBER_DAYS_VISIBLE * 2) + -daysScrolledSoFar) {
                dayXPos = TIME_COLUMN_PADDING + accumulatedScrollOffset.x + widthPerDay * day;
                dayLines[i1] = dayXPos;
                dayLines[i2] = HEADER_TEST_TOP_PADDING;
                dayLines[i3] = dayXPos;
                dayLines[i4] = PARENT_HEIGHT;
                headerDayLines[i1] = dayXPos;
                headerDayLines[i2] = HEADER_TEST_TOP_PADDING;
                headerDayLines[i3] = dayXPos;
                headerDayLines[i4] = HEADER_HEIGHT;
                day++;
            }
            hourLines[i1] = TIME_COLUMN_PADDING;
            hourLines[i2] = top;
            hourLines[i3] = PARENT_WIDTH;
            hourLines[i4] = top;
            i++;
        }
    }


}