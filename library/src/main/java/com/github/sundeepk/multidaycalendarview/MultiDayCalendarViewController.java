package com.github.sundeepk.multidaycalendarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.OverScroller;

import com.github.sundeepk.multidaycalendarview.R;
import com.github.sundeepk.multidaycalendarview.domain.Event;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MultiDayCalendarViewController implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final int NONE = 0;
    private static final int HORIZONTAL = 1;
    private static final int VERTICAL = 2;
    private static final int TIME_COLUMN_PADDING = 80;
    private static final int HEADER_HEIGHT = 200;
    private static final int SECS_IN_DAY = 86400;
    private static final int SECS_IN_HOUR = 60 * 60;
    private static final int EVENT_PADDING = 5;
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_MAX_OFF_PATH = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;

    private Paint timeColumnPaint;
    private Paint dayHourSeparatorPaint;
    private TextPaint eventsPaint;
    private OverScroller scroller;
    private PointF accumulatedScrollOffset = new PointF(0f, 0f);
    private Locale locale = Locale.getDefault();
    private Calendar currentCalendar = Calendar.getInstance(locale);
    private Date currentDate = new Date();
    private Map<Long, Event<?>> epochSecsToEvents = new HashMap<>();
    private Rect timeColumnRect = new Rect();
    private MultiDayCalendarView.MultiDayCalendarListener multiDayCalendarListener;

    private int currentFlingDirection;
    private int currentScrollDirection = NONE;
    private int timeTextWidth;
    private int timeTextHeight;
    private int headerTextTopPadding;
    private int widthPerDay;
    private int numberOfVisibleDays = 3;
    private float distanceX;
    private float distanceY;
    private boolean isFling;
    private int minYScrollAllowed;
    private float prevDiff;
    private boolean isFingerLifted;
    private float[] hourLines = new float[24 * 4];
    private float[] dayLines;
    private float[] headerDayLines;
    private int daysScrolledSoFar;
    private int width;
    private int height;
    private RectF helperRect;
    private int eventRectTextHeight;
    private int eventTextSize = 25;
    private int dateHeaderTextSize = 25;

    MultiDayCalendarViewController(Paint dayHourSeparatorPaint, Paint timeColumnPaint,
                                    TextPaint eventsPaint, OverScroller scroller, RectF helperRect,
                                    Rect textRect, AttributeSet attrs, Context context) {
        this.dayHourSeparatorPaint = dayHourSeparatorPaint;
        this.timeColumnPaint = timeColumnPaint;
        this.eventsPaint = eventsPaint;
        this.scroller = scroller;
        this.helperRect = helperRect;
        loadAttributes(attrs, context);
        init(textRect);
    }

    void setMultiDayCalendarListener(MultiDayCalendarView.MultiDayCalendarListener multiDayCalendarListener){
        this.multiDayCalendarListener = multiDayCalendarListener;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if(multiDayCalendarListener != null ){
            int hour = (int) ((e.getY() - HEADER_HEIGHT - accumulatedScrollOffset.y) / timeTextHeight);
            int day = Math.round((e.getX() / widthPerDay) - daysScrolledSoFar) - 1;
            currentCalendar.setTime(currentDate);
            plusHoursAndDays(hour, day);
            long eventStartTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(currentCalendar.getTimeInMillis());
            if(epochSecsToEvents.containsKey(eventStartTimeSeconds)){
                multiDayCalendarListener.onEventSelect(eventStartTimeSeconds, epochSecsToEvents.get(eventStartTimeSeconds));
            }else{
                multiDayCalendarListener.onNewEventCreate(eventStartTimeSeconds);
            }
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        scroller.forceFinished(true);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        scroller.forceFinished(true);
            isFling = true;
        if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
            scroller.fling(0, (int) accumulatedScrollOffset.y, 0, (int) velocityY, 0, 0, minYScrollAllowed, 0);

        } else if (Math.abs(e2.getY() - e1.getY()) > SWIPE_MAX_OFF_PATH) {
            scroller.fling(0, (int) accumulatedScrollOffset.y, 0, (int) velocityY, 0, 0, minYScrollAllowed, 0);

        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        isFling = false;
        if (currentScrollDirection == NONE) {
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                currentScrollDirection = HORIZONTAL;
                currentFlingDirection = HORIZONTAL;
            } else {
                currentScrollDirection = VERTICAL;
                currentFlingDirection = VERTICAL;
            }
        }

        this.distanceX = distanceX;
        this.distanceY = distanceY;
        return true;
    }

    private void loadAttributes(AttributeSet attrs, Context context) {
        if(attrs != null && context != null){
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,  R.styleable.MultiDayCalendarView, 0, 0);
            try{
                eventTextSize = typedArray.getDimensionPixelSize(R.styleable.MultiDayCalendarView_multiDayCalendarEventTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, eventTextSize, context.getResources().getDisplayMetrics()));
                dateHeaderTextSize = typedArray.getDimensionPixelSize(R.styleable.MultiDayCalendarView_multiDayCalendarDateHeaderTextSize,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, eventTextSize, context.getResources().getDisplayMetrics()));
            }finally{
                typedArray.recycle();
            }
        }
    }

    private void init(Rect textRect) {
        dayHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        dayHourSeparatorPaint.setStrokeWidth(2);
        dayHourSeparatorPaint.setARGB(255, 219, 219, 219);

        headerTextTopPadding = 60;

        timeColumnPaint.setTextAlign(Paint.Align.LEFT);
        timeColumnPaint.setTextSize(dateHeaderTextSize);
        timeColumnPaint.getTextBounds("00 PM", 0, "00 PM".length(), textRect);
        timeTextHeight = textRect.height() * 6;
        timeTextWidth = (int) timeColumnPaint.measureText("00 PM");
        timeColumnPaint.setTypeface(Typeface.SANS_SERIF);
        timeColumnPaint.setFlags(Paint.ANTI_ALIAS_FLAG);


        eventsPaint.setStyle(Paint.Style.FILL);
        eventsPaint.setColor(Color.BLUE);
        eventsPaint.setTextSize(eventTextSize);
        eventsPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        eventsPaint.setTypeface(Typeface.DEFAULT_BOLD);
        eventsPaint.getTextBounds("00 PM", 0, "00 PM".length(), textRect);
        eventRectTextHeight = textRect.height() + 5; // add some extra padding so that lines aren't drawn too close

        dayLines = new float[(numberOfVisibleDays) * 4 * 3];
        headerDayLines = new float[(numberOfVisibleDays) * 4 * 3];
    }

    protected void onMeasure(int parentWidth, int parentHeight) {
        width = parentWidth;
        height = parentHeight;
        int padding = 10;
        int headerColumnWidth = timeTextWidth + padding * 2;
        widthPerDay = parentWidth - headerColumnWidth - padding * (numberOfVisibleDays);
        widthPerDay = widthPerDay / numberOfVisibleDays;
        minYScrollAllowed = -(timeTextHeight * 24 + HEADER_HEIGHT - parentHeight);
    }

    protected void onDraw(Canvas canvas) {
        calculateXAxisOffset();

        // fill arrays with the lines needed to draw hours and days much like a grid
        fillDayAndHourLines();

        drawDayAndHourLines(canvas);

        drawDayHeaderAndTimeColumn(canvas);

        drawTopLeftRect(canvas);

        calculateYAxisOffset();
    }

    private void calculateXAxisOffset() {
        if (!isFingerLifted && currentScrollDirection == HORIZONTAL) {
            accumulatedScrollOffset.x -= distanceX;
        }
    }

    private void calculateYAxisOffset() {
        boolean isUp = false;
        if (Math.abs(accumulatedScrollOffset.y - distanceY) > prevDiff) {
            isUp = true;
        }
        if (currentScrollDirection == VERTICAL) {
            if (accumulatedScrollOffset.y - distanceY > 0) {
                accumulatedScrollOffset.y = 0;
            } else if (accumulatedScrollOffset.y - distanceY < minYScrollAllowed) {
                accumulatedScrollOffset.y = minYScrollAllowed;
            } else if (isFling && !isUp && (accumulatedScrollOffset.y - distanceY) > scroller.getFinalY()) {
                accumulatedScrollOffset.y -= distanceY;
            } else if (isFling && isUp && (accumulatedScrollOffset.y - distanceY) < scroller.getFinalY()) {
                accumulatedScrollOffset.y -= distanceY;
            } else if (!isFling) {
                accumulatedScrollOffset.y -= distanceY;
            }
        }
        prevDiff = Math.abs(accumulatedScrollOffset.y - distanceY);
    }

    private void drawTopLeftRect(Canvas canvas) {
        timeColumnRect.top = 0;
        timeColumnRect.left = 0;
        timeColumnRect.bottom = HEADER_HEIGHT;
        timeColumnRect.right = TIME_COLUMN_PADDING;
        timeColumnPaint.setStyle(Paint.Style.FILL);
        timeColumnPaint.setColor(Color.WHITE);
        canvas.drawRect(timeColumnRect, timeColumnPaint);
    }

    private void drawDayHeaderAndTimeColumn(Canvas canvas) {
        timeColumnPaint.setStyle(Paint.Style.STROKE);
        timeColumnPaint.setARGB(255, 117, 117, 117);
        for (int hour = 0, day = -numberOfVisibleDays + -(daysScrolledSoFar); hour < 24; hour++) {
            float top = accumulatedScrollOffset.y + timeTextHeight * hour + HEADER_HEIGHT + 10;
            if (day < (numberOfVisibleDays * 2) + -daysScrolledSoFar) {
                float dayXPos = TIME_COLUMN_PADDING + accumulatedScrollOffset.x + widthPerDay * day + 10;
                currentCalendar.setTime(currentDate);
                plusDays(day);
                Date date = currentCalendar.getTime();
                timeColumnPaint.setTextSize(45);
                canvas.drawText(Integer.toString(currentCalendar.get(Calendar.DAY_OF_MONTH)), dayXPos, headerTextTopPadding, timeColumnPaint);
                timeColumnPaint.setTextSize(30);
                canvas.drawText(currentCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale),
                        dayXPos, headerTextTopPadding + 35, timeColumnPaint);
                day++;
            }
            if (hour > 0) {
                timeColumnPaint.setTextSize(25);
                if (hour > 11) {
                    canvas.drawText(Integer.toString(hour) + "pm", 7, top, timeColumnPaint);
                } else {
                    canvas.drawText(Integer.toString(hour) + "am", 14, top, timeColumnPaint);
                }
            }
        }
    }

    private void plusHoursAndDays(int hour, int day) {
        currentCalendar.set(Calendar.HOUR_OF_DAY, hour);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);
        currentCalendar.add(Calendar.DATE, day);
    }


    private void plusDays(int day) {
        plusHoursAndDays(0, day);
    }

    private void drawDayAndHourLines(Canvas canvas) {
        canvas.drawLines(dayLines, dayHourSeparatorPaint);

        // draw all the events rects on the canvas
        drawEventRects(canvas);

        timeColumnRect.top = 0;
        timeColumnRect.left = 0;
        timeColumnRect.bottom = height;
        timeColumnRect.right = TIME_COLUMN_PADDING;
        timeColumnPaint.setStyle(Paint.Style.FILL);
        timeColumnPaint.setColor(Color.WHITE);
        canvas.drawRect(timeColumnRect, timeColumnPaint);


        canvas.drawLines(hourLines, dayHourSeparatorPaint);

        timeColumnRect.top = 0;
        timeColumnRect.left = TIME_COLUMN_PADDING;
        timeColumnRect.bottom = HEADER_HEIGHT;
        timeColumnRect.right = width;
        canvas.drawRect(timeColumnRect, timeColumnPaint);

        //draw the header
        canvas.drawLines(headerDayLines, dayHourSeparatorPaint);
    }

    private void fillDayAndHourLines() {
        float dayXPos;
        for (int step = 0, i = 0, day = -numberOfVisibleDays + -(daysScrolledSoFar); step < 24; step++) {
            float top = accumulatedScrollOffset.y + timeTextHeight * step + (HEADER_HEIGHT);
            int i1 = i * 4;
            int i2 = i1 + 1;
            int i3 = i1 + 2;
            int i4 = i1 + 3;
            if (day < (numberOfVisibleDays * 2) + -daysScrolledSoFar) {
                dayXPos = TIME_COLUMN_PADDING + accumulatedScrollOffset.x + widthPerDay * day;
                fillDayLines(dayXPos, i1, i2, i3, i4);
                fillHeaderDayLines(dayXPos, i1, i2, i3, i4);
                day++;
            }
            fillHourLines(top, i1, i2, i3, i4);
            i++;
        }
    }

    private void fillHourLines(float top, int i1, int i2, int i3, int i4) {
        hourLines[i1] = TIME_COLUMN_PADDING;
        hourLines[i2] = top;
        hourLines[i3] = width;
        hourLines[i4] = top;
    }

    private void fillHeaderDayLines(float dayXPos, int i1, int i2, int i3, int i4) {
        headerDayLines[i1] = dayXPos;
        headerDayLines[i2] = headerTextTopPadding;
        headerDayLines[i3] = dayXPos;
        headerDayLines[i4] = HEADER_HEIGHT;
    }

    private void fillDayLines(float dayXPos, int i1, int i2, int i3, int i4) {
        dayLines[i1] = dayXPos;
        dayLines[i2] = headerTextTopPadding;
        dayLines[i3] = dayXPos;
        dayLines[i4] = height;
    }

    private void drawEventRects(Canvas canvas) {
        int tmpDaysScrolledSoFar = (int)(accumulatedScrollOffset.x / widthPerDay);
        // update days scrolled so that when scrolling in x-axis we always draw events with nearest next day.
        // This prevents events from just popping up out of no where as well.
        Date startDateTime;
        long endDate = 0;
        currentCalendar.setTime(currentDate);
        if(tmpDaysScrolledSoFar <= 0){
            tmpDaysScrolledSoFar+=1; // offset tmpDaysScrolledSoFar to prevent random popping up of events, to give a sufficient window to draw with
            plusDays(-tmpDaysScrolledSoFar);
            startDateTime = currentCalendar.getTime();

            currentCalendar.setTime(startDateTime);
            plusDays(numberOfVisibleDays + 2);
            endDate = currentCalendar.getTimeInMillis() / 1000;
        }else{
            tmpDaysScrolledSoFar+=numberOfVisibleDays;
            plusDays(-tmpDaysScrolledSoFar);
            startDateTime = currentCalendar.getTime();

            currentCalendar.setTime(startDateTime);
            plusDays(numberOfVisibleDays + numberOfVisibleDays + 2);
            endDate = currentCalendar.getTimeInMillis() / 1000;
        }

        float scrolledHour = Math.abs(accumulatedScrollOffset.y / timeTextHeight);

        //create a window for which events can be drawn. Prevents unnecessary event rects being drawn.
        long startTime = startDateTime.getTime() / 1000;
        if (endDate < startTime) {
            long tmp = endDate;
            endDate = startTime;
            startTime = tmp;
        }
        int maxNumberOfLines = (timeTextHeight / eventRectTextHeight) - 1; // minus 1 because we offset the drawing of text by eventRectTextHeight / 2
        int padding = eventRectTextHeight / 2;
        for (Map.Entry<Long, Event<?>> events : epochSecsToEvents.entrySet()) {
            long timeInSeconds = events.getKey();
            if (timeInSeconds >= startTime && timeInSeconds <= endDate) {
                Event event = events.getValue();
                long difference;
                if (timeInSeconds > startTime) {
                    difference = (timeInSeconds - startTime);
                } else {
                    difference = (startTime - timeInSeconds);
                }
                float dayX = TIME_COLUMN_PADDING + accumulatedScrollOffset.x + widthPerDay * ((difference / SECS_IN_DAY) + -tmpDaysScrolledSoFar);
                float hour = Math.round(accumulatedScrollOffset.y + timeTextHeight * ((difference / SECS_IN_HOUR) % 24) + HEADER_HEIGHT);
                eventsPaint.setColor(event.getColor());
                helperRect.left = dayX + EVENT_PADDING;
                helperRect.top = hour;
                helperRect.right = (dayX + widthPerDay);
                helperRect.bottom = (hour + timeTextHeight) - EVENT_PADDING;
                canvas.drawRoundRect(helperRect, 6, 6, eventsPaint);
                drawEventText(canvas, maxNumberOfLines, event, helperRect.left, helperRect.top + (eventRectTextHeight + padding));
            }
        }
    }

    private void drawEventText(Canvas canvas, int maxNumberOfLines, Event event, float dayX, float hourY) {
        String eventText = event.getEventName();
        int count =0;
        int start =0;
        int heightOffset = 0;
        eventsPaint.setColor(Color.WHITE);
        // line wrap the text to draw
        while (count < maxNumberOfLines && start < eventText.length()) {
            int end = eventsPaint.breakText(eventText, start, eventText.length(), true, widthPerDay, null);

            if (end <= 0) {
                canvas.drawText(eventText, start, start + end, dayX, hourY + heightOffset, eventsPaint);
                break;
            }

            int startOfCurrentWord = event.hasWhiteSpaceInEventName() ? getStartOfWordPos(eventText, start + end) : end;
            int endOfCurrentWord = getEndOfWordPos(eventText, start + end);

            if (endOfCurrentWord >= (start + end)) {
                canvas.drawText(eventText, start, startOfCurrentWord, dayX, hourY + heightOffset, eventsPaint);
                start = startOfCurrentWord;
            } else {
                canvas.drawText(eventText, start, start + end, dayX, hourY + heightOffset, eventsPaint);
                start = start + end;
            }

            count++;
            heightOffset += eventRectTextHeight;
        }
    }

    private int getEndOfWordPos(String eventText, int position) {
        int textLength = eventText.length();
        if(position+1 < textLength && eventText.charAt(position) != ' '){
            while(position < textLength && position > 0 && eventText.charAt(position + 1) != ' '){
                position++;
            }
        }
        return position;
    }

    private int getStartOfWordPos(String eventText, int position) {
        int textLength = eventText.length();
        if(position+1 < textLength && eventText.charAt(position) != ' '){
            while(position < textLength && position > 0 && eventText.charAt(position - 1) != ' '){
                position--;
            }
        }
        return position;
    }

    // return true whether scroll is happening
    public boolean computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (currentFlingDirection == VERTICAL) {
                accumulatedScrollOffset.y = scroller.getCurrY();
            } else {
                accumulatedScrollOffset.x = scroller.getCurrX();
            }
            return true;
        }
        return false;
    }

    // returns true whether an event it was interested in happened
    protected boolean onTouchEvent(MotionEvent event, GestureDetectorCompat gestureDetectorCompat) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (currentScrollDirection == HORIZONTAL) {
                daysScrolledSoFar = Math.round(accumulatedScrollOffset.x / (widthPerDay));
                float remainingScrollAfterFingerLifted = (accumulatedScrollOffset.x - daysScrolledSoFar * (widthPerDay));
                scroller.startScroll((int) accumulatedScrollOffset.x, 0, (int) -remainingScrollAfterFingerLifted, 0);
                currentScrollDirection = NONE;
                if(multiDayCalendarListener != null){
                    currentCalendar.setTime(currentDate);
                    plusDays(-daysScrolledSoFar);
                    multiDayCalendarListener.onCalendarScroll(currentCalendar.getTime());
                }
                return true;
            }else if(currentScrollDirection == VERTICAL){
                currentScrollDirection = NONE;
                return gestureDetectorCompat.onTouchEvent(event);
            }
        }
        return gestureDetectorCompat.onTouchEvent(event);
    }

    public Date getCurrentLeftMostDay() {
        currentCalendar.setTime(currentDate);
        plusDays(-daysScrolledSoFar);
        return currentCalendar.getTime();
    }

    void addEvents(Map<Long, Event<?>> eventsToAdd) {
        this.epochSecsToEvents.putAll(eventsToAdd);
    }

    void addEvent(long epochTimeInSecsToTheClosetHour, Event event){
        this.epochSecsToEvents.put(epochTimeInSecsToTheClosetHour, event);
    }

    Event removeEvent(long epochTimeInSecsToTheClosetHour){
        return this.epochSecsToEvents.remove(epochTimeInSecsToTheClosetHour);
    }

    boolean containsEvent(long epochTimeInSecsToTheClosetHour){
        return this.epochSecsToEvents.containsKey(epochTimeInSecsToTheClosetHour);
    }

    void gotoDate(Date dateToGoTo){
        currentDate = new Date(dateToGoTo.getTime());
        currentCalendar.setTime(currentDate);
        daysScrolledSoFar = 0;
        accumulatedScrollOffset.x = 0;
        distanceX = 0;
    }

    void setEventsMap(Map<Long, Event<?>> eventsMap){
        this.epochSecsToEvents = eventsMap;
    }

}
