package com.github.sundeepk.multidaycalendarview.domain;

public class Event<T>{

    private final String eventName;
    private final boolean hasWhiteSpace;
    private final T data;
    private final int color;

    public Event(final String eventName, final T data, final int color) {
        this.eventName = eventName;
        this.data = data;
        this.hasWhiteSpace = hasWhiteSpace(eventName);
        this.color = color;
    }

    public Event(final String eventName, final T data, final int color, boolean hasWhiteSpaceInEventName) {
        this.eventName = eventName;
        this.data = data;
        this.hasWhiteSpace = hasWhiteSpaceInEventName;
        this.color = color;
    }

    private boolean hasWhiteSpace(CharSequence sequence) {
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (sequence.charAt(i) == ' ') {
                return true;
            }
        }
        return false;
    }

    public int getColor() {
        return color;
    }

    public String getEventName() {
        return eventName;
    }

    public boolean hasWhiteSpaceInEventName() {
        return hasWhiteSpace;
    }

    public T getData(){
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (color != event.color) return false;
        if (hasWhiteSpace != event.hasWhiteSpace) return false;
        if (data != null ? !data.equals(event.data) : event.data != null) return false;
        if (eventName != null ? !eventName.equals(event.eventName) : event.eventName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventName != null ? eventName.hashCode() : 0;
        result = 31 * result + (hasWhiteSpace ? 1 : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + color;
        return result;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventName='" + eventName + '\'' +
                ", hasWhiteSpace=" + hasWhiteSpace +
                ", data=" + data +
                ", color=" + color +
                '}';
    }
}