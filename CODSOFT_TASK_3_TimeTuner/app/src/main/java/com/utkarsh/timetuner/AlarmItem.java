package com.utkarsh.timetuner;

import java.util.Calendar;

public class AlarmItem {
    private int hour;
    private int minute;
    private String alarmSound; // Add this field

    public AlarmItem(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        this.alarmSound = alarmSound;
    }

    // Getter and setter methods for the new field
    public String getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(String alarmSound) {
        this.alarmSound = alarmSound;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getFormattedTime() {
        return String.format("%02d:%02d", hour, minute);
    }

    public Calendar getAlarmTime() {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);
        return alarmTime;
    }
}
