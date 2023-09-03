package com.utkarsh.todo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String task = intent.getStringExtra("task");
        showNotification(context, task);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context context, String task) {
        // Build and show the notification using NotificationCompat.Builder
        // Make sure you provide a valid small icon resource for the notification

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setContentTitle("Reminder: " + task)
                .setContentText("It's time to complete your task!")
                .setSmallIcon(R.drawable.notification) // Set a valid small icon resource
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(/*notification_id*/ 0, builder.build());
    }
}

