package com.github.sundeepk.multidaycalendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import com.github.sundeepk.multidaycalendarview.domain.Event;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MultiDayCalendarView extends View {

    private MultiDayCalendarViewController multiDayCalendarViewController;
    private GestureDetectorCompat gestureDetector;

    private final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            multiDayCalendarViewController.onSingleTapConfirmed(e);
            invalidate();
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            multiDayCalendarViewController.onFling(e1, e2, velocityX, velocityY);
            invalidate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            multiDayCalendarViewController.onScroll(e1, e2, distanceX, distanceY);
            invalidate();
            return true;
        }
    };

    public interface MultiDayCalendarListener {
       public void onNewEventCreate(long eventStartDateTime);
       public void onEventSelect(long eventStartDateTime, Event selectedEvent);
       public void onCalendarScroll(Date firstDateTimeShownInCalendarView);
    }

    public MultiDayCalendarView(Context context) {
        this(context, null);
    }

    public MultiDayCalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiDayCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setLocale(Locale locale){
        multiDayCalendarViewController.setLocale(locale);
    }

    public void setTimeZoneAndLocale(TimeZone timeZone, Locale locale){
        multiDayCalendarViewController.setTimeZoneAndLocale(timeZone, locale);
    }

    public void setCalendarListener(MultiDayCalendarListener listener){
        multiDayCalendarViewController.setMultiDayCalendarListener(listener);
    }

    private void init(Context context, AttributeSet attrs) {
        OverScroller scroller = new OverScroller(getContext(), new DecelerateInterpolator());
        multiDayCalendarViewController = new MultiDayCalendarViewController(new Paint(),
                new Paint(), new TextPaint(), scroller, new RectF(), new Rect(), attrs, context);
        gestureDetector = new GestureDetectorCompat(getContext(), gestureListener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        multiDayCalendarViewController.onMeasure(getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        multiDayCalendarViewController.onMeasure(getWidth(), getHeight());
        multiDayCalendarViewController.onDraw(canvas);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (multiDayCalendarViewController.computeScroll()) {
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = multiDayCalendarViewController.onTouchEvent(event, gestureDetector);
        invalidate();
        return result;
    }

     public void addEvents(Map<Long, Event<?>> eventsToAdd){
        if(eventsToAdd != null && !eventsToAdd.isEmpty()){
            multiDayCalendarViewController.addEvents(eventsToAdd);
            invalidate();
        }
    }

    public void addEvent(long epochTimeInSecsToTheClosetHour, Event event){
        if(event != null && epochTimeInSecsToTheClosetHour > 0){
            this.multiDayCalendarViewController.addEvent(epochTimeInSecsToTheClosetHour, event);
            invalidate();
        }
    }

    public Event removeEvent(long epochTimeInSecsToTheClosetHour){
        Event event = this.multiDayCalendarViewController.removeEvent(epochTimeInSecsToTheClosetHour);
        if(event != null){
            invalidate();
        }
        return event;
    }

    public boolean containsEvent(long epochTimeInSecsToTheClosetHour){
        return this.multiDayCalendarViewController.containsEvent(epochTimeInSecsToTheClosetHour);
    }

    public void goToDate(Date dateToGoTo){
        multiDayCalendarViewController.gotoDate(dateToGoTo);
        invalidate();
    }

    public Date getCurrentLeftMostDay(){
        return multiDayCalendarViewController.getCurrentLeftMostDay();
    }

    public void setEventMap(){

    }

}