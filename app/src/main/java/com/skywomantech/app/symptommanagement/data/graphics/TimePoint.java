package com.skywomantech.app.symptommanagement.data.graphics;

import java.util.Calendar;

public class TimePoint {
    long timeValue;
    long actual_date;
    int hour;
    int day_of_week;
    int day_of_month;

    public TimePoint() {
    }

    public TimePoint(long timeValue) {
        this.timeValue = timeValue;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeValue);
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.day_of_week = cal.get(Calendar.DAY_OF_WEEK);
        this.day_of_month = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.actual_date = cal.getTimeInMillis();
    }

    public long getTimeValue() {
        return timeValue;
    }
    public void setTimeValue(long timeValue) {
        this.timeValue = timeValue;
    }
    public int getHour() {
        return hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public int getDay_of_week() {
        return day_of_week;
    }
    public void setDay_of_week(int day_of_week) {
        this.day_of_week = day_of_week;
    }
    public int getDay_of_month() {
        return day_of_month;
    }
    public void setDay_of_month(int day_of_month) {
        this.day_of_month = day_of_month;
    }

    public long getActual_date() {
        return actual_date;
    }

    public void setActual_date(long actual_date) {
        this.actual_date = actual_date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimePoint)) return false;

        TimePoint timePoint = (TimePoint) o;

        if (timeValue != timePoint.timeValue) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (timeValue ^ (timeValue >>> 32));
    }

    @Override
    public String toString() {
        return "TimePoint{" +
                "timeValue=" + timeValue +
                ", actual_date=" + actual_date +
                ", hour=" + hour +
                ", day_of_week=" + day_of_week +
                ", day_of_month=" + day_of_month +
                '}';
    }
}
