package com.utkarsh.timetuner;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private TimePicker timePicker;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private Ringtone alarmTone;
    private ArrayList<AlarmItem> alarmList;
    private AlarmAdapter alarmAdapter;
    private ListView alarmListView;
    private Button stopAlarmButton;
    private Handler handler;
    private boolean isAlarmRinging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmListView = findViewById(R.id.alarmListView);
        stopAlarmButton = findViewById(R.id.stopAlarmButton);

        // Initialize the list of alarms and the adapter
        alarmList = new ArrayList<>();
        alarmAdapter = new AlarmAdapter(this, alarmList);
        alarmListView.setAdapter(alarmAdapter);

        // Hide the ListView if it's empty
        if (alarmList.isEmpty()) {
            alarmListView.setVisibility(View.GONE);
        }

        stopAlarmButton = findViewById(R.id.stopAlarmButton);
        stopAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAlarm();
            }
        });

        // Set up the alarm tone (replace with your own alarm sound)
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        alarmTone = RingtoneManager.getRingtone(getApplicationContext(), alarmSound);

        // Initialize the handler for checking alarms
        handler = new Handler();
        isAlarmRinging = false;
        checkAlarms(); // Check alarms immediately when the app starts
    }

    public void setAlarm(View view) {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

        // Schedule the alarm to repeat
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        // Add the alarm item to the list
        AlarmItem alarmItem = new AlarmItem(hour, minute);
        alarmList.add(alarmItem);
        alarmAdapter.notifyDataSetChanged();

        // Show the ListView
        alarmListView.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Alarm set for " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
    }

    // Function to check and trigger alarms
    private void checkAlarms() {
        if (!isAlarmRinging) {
            Calendar currentTime = Calendar.getInstance();

            for (AlarmItem alarmItem : alarmList) {
                Calendar alarmTime = alarmItem.getAlarmTime();

                if (alarmTime != null && currentTime.compareTo(alarmTime) >= 0) {
                    // Trigger the alarm
                    triggerAlarm(alarmItem);
                    break; // Stop checking once the first alarm is triggered
                }
            }
        }

        // Re-schedule the alarm checking after a delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAlarms();
            }
        }, 1000); // Check every second
    }

    // Function to trigger an alarm and show a notification
    private void triggerAlarm(AlarmItem alarmItem) {
        isAlarmRinging = true; // Set the flag to indicate an alarm is ringing
        if (!alarmTone.isPlaying()) {
            alarmTone.play();
        }

        // Show a notification
        createNotification("Alarm", "Alarm for " + alarmItem.getFormattedTime() + " is ringing.");
    }

    // Function to create a notification
    @SuppressLint("MissingPermission")
    private void createNotification(String title, String content) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "alarm_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }


    // Function to create a notification channel (for Android Oreo and higher)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Alarm Channel";
            String description = "Channel for alarm notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("alarm_channel", name, importance);
            channel.setDescription(description);

            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build();
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes);
            }

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void stopAlarm() {
        if (alarmTone != null && alarmTone.isPlaying()) {
            alarmTone.stop();
            isAlarmRinging = false; // Reset the flag when the alarm is stopped

            // Cancel the pending intent to prevent it from firing again
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        }

        // Remove the alarm item from the list
        if (!alarmList.isEmpty()) {
            alarmList.remove(alarmList.size() - 1);
            alarmAdapter.notifyDataSetChanged();
        }

        // Hide the ListView if it's empty
        if (alarmList.isEmpty()) {
            alarmListView.setVisibility(View.GONE);
        }
    }

}
