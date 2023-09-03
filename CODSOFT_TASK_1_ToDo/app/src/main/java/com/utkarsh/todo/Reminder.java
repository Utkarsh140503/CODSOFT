package com.utkarsh.todo;

public class Reminder {
    private String taskTitle;
    private long reminderTimeMillis;

    public Reminder(String taskTitle, long reminderTimeMillis) {
        this.taskTitle = taskTitle;
        this.reminderTimeMillis = reminderTimeMillis;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public long getReminderTimeMillis() {
        return reminderTimeMillis;
    }
}