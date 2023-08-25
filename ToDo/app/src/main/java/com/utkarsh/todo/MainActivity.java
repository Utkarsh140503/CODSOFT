package com.utkarsh.todo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private List<String> tasksList;
    private ArrayAdapter<String> tasksAdapter;
    private ListView taskListView;
    private TextView tvNoTasks;
    private LinearLayout developerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        tasksList = new ArrayList<>();
        taskListView = findViewById(R.id.taskListView);
        tasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasksList);
        taskListView.setAdapter(tasksAdapter);
        tvNoTasks = findViewById(R.id.tvNoTasks);

        developerLayout = findViewById(R.id.developerLayout);

        developerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portfolioUrl = "https://utkarsh140503.github.io/Portfolio/#";
                Uri uri = Uri.parse(portfolioUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTaskOptions(position);
            }
        });

        ImageView btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddTaskDialog();
            }
        });

        updateTasksList(); // Load tasks from the database initially
    }

    private void openAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Set the positive button's click listener after showing the dialog
        Button btnSaveTask = dialogView.findViewById(R.id.btnSaveTask);
        btnSaveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
                EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
                String taskTitle = etTaskTitle.getText().toString();
                String taskDescription = etTaskDescription.getText().toString();

                // Insert task into the database
                insertTask(taskTitle, taskDescription);

                // Refresh the ListView
                updateTasksList();

                dialog.dismiss(); // Close the dialog after saving
            }
        });
    }

    private void showTaskOptions(final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View optionsView = inflater.inflate(R.layout.dialog_task_options, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(optionsView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        ListView lvTaskOptions = optionsView.findViewById(R.id.lvTaskOptions);
        String[] options = new String[]{"Edit Task", "Delete Task", "Set Reminder", "Completed!"};
        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        lvTaskOptions.setAdapter(optionsAdapter);

        lvTaskOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                dialog.dismiss();
                if (which == 0) {
                    openEditTaskDialog(position);
                } else if (which == 1) {
                    deleteTask(position);
                } else if (which == 2) {
                    String taskTitle = tasksList.get(position).split("\n")[0].replace("Task Title: ", "");
                    setReminder(taskTitle); // Pass the taskTitle to the setReminder() method
                }else if (which == 3) {
                    completeTask(position);
                }
            }
        });
    }

    private void completeTask(int position) {
        String taskInfo = tasksList.get(position);
        String[] taskInfoParts = taskInfo.split("\\. ", 2);
        if (taskInfoParts.length == 2) {
            String taskTitle = taskInfoParts[1].split(":")[0].trim();
            String taskDescription = taskInfoParts[1].split(":").length > 1 ? taskInfoParts[1].split(":")[1].trim() : "";

            // Move the task to the completed tasks table
            insertCompletedTask(taskTitle, taskDescription);

            // Delete the task from the tasks table
            deleteTask(position);

            Toast.makeText(this, "Task completed!\nView all completed task by selecting View Completed Tasks", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertCompletedTask(String title, String description) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        db.insert(DatabaseHelper.TABLE_COMPLETED_TASKS, null, values);
        db.close();
    }

    private void setReminder(final String task) {
        final String taskTitle = task.split("\n")[0].replace("Task Title: ", "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Reminder");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_reminder, null);
        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
                selectedTime.set(Calendar.SECOND, 0);

                Calendar currentTime = Calendar.getInstance();

                if (selectedTime.before(currentTime)) {
                    // If the selected time is earlier than the current time, set it for the next day
                    selectedTime.add(Calendar.DAY_OF_MONTH, 1);
                }

                long reminderTimeInMillis = selectedTime.getTimeInMillis();

                long timeDifferenceInMillis = reminderTimeInMillis - currentTime.getTimeInMillis();

                if (!hasNotificationPermission()) {
                    requestNotificationPermission();
                } else {
                    createReminder(taskTitle, reminderTimeInMillis);
                    showReminderTimeInfo(timeDifferenceInMillis);
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showReminderTimeInfo(long timeDifferenceInMillis) {
        long seconds = timeDifferenceInMillis / 1000;
        if (seconds < 60) {
            Toast.makeText(MainActivity.this, "Reminder set for " + seconds + " seconds later", Toast.LENGTH_LONG).show();
        } else {
            long minutes = seconds / 60;
            if (minutes < 60) {
                Toast.makeText(MainActivity.this, "Reminder set for " + minutes + " minutes later", Toast.LENGTH_LONG).show();
            } else {
                long hours = minutes / 60;
                long remainingMinutes = minutes % 60;
                String timeInfo = String.format("Reminder set for %d hours and %d minutes later", hours, remainingMinutes);
                Toast.makeText(MainActivity.this, timeInfo, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all_tasks) {
            confirmDeleteAllTasks();
            return true;
        }

        if (id == R.id.action_view_completed_tasks) {
            Intent intent = new Intent(this, CompletedTasksActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteAllTasks() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete all tasks?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAllTasks();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteAllTasks() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_TASKS, null, null);
        db.close();

        tasksList.clear();
        tasksAdapter.notifyDataSetChanged();

        if (tasksList.isEmpty()) {
            taskListView.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            taskListView.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);
        }
    }

    private boolean hasNotificationPermission() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        return notificationManager.areNotificationsEnabled();
    }

    private void requestNotificationPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Notification Permission Required");
        builder.setMessage("This app requires notification permission to set reminders. Please grant the permission in the app settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createReminder(String taskTitle, long reminderTimeInMillis) {
        SharedPreferences sharedPreferences = getSharedPreferences("reminders", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store the reminder time with the task title as a single string
        String reminderValue = taskTitle + "|" + reminderTimeInMillis;
        editor.putString(taskTitle, reminderValue);
        editor.apply();

        Intent reminderIntent = new Intent(MainActivity.this, ReminderBroadcastReceiver.class);
        reminderIntent.putExtra("taskTitle", taskTitle);

        int requestCode = (int) System.currentTimeMillis(); // Use a unique request code
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, requestCode, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE); // Use FLAG_IMMUTABLE here

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);
    }

    private void openEditTaskDialog(final int position) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Extract the serial number, task title, and task description
        String taskInfo = tasksList.get(position);
        String[] taskInfoParts = taskInfo.split("\\. ", 2);
        if (taskInfoParts.length == 2) {
            final int serialNumber = Integer.parseInt(taskInfoParts[0]);
            final String originalTaskTitle = taskInfoParts[1].split(":")[0].trim();
            final String originalTaskDescription = taskInfoParts[1].split(":").length > 1 ? taskInfoParts[1].split(":")[1].trim() : "";

            // Populate EditText fields with task details
            EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
            EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
            etTaskTitle.setText(originalTaskTitle);
            etTaskDescription.setText(originalTaskDescription);

            // Set the positive button's click listener after showing the dialog
            Button btnSaveTask = dialogView.findViewById(R.id.btnSaveTask);
            btnSaveTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText etTaskTitle = dialogView.findViewById(R.id.etTaskTitle);
                    EditText etTaskDescription = dialogView.findViewById(R.id.etTaskDescription);
                    String newTaskTitle = etTaskTitle.getText().toString();
                    String newTaskDescription = etTaskDescription.getText().toString();

                    // Update task in the database
                    updateTask(serialNumber, originalTaskTitle, originalTaskDescription, newTaskTitle, newTaskDescription);

                    // Refresh the ListView
                    updateTasksList();

                    dialog.dismiss(); // Close the dialog after saving
                }
            });
        }
    }

    private void updateTask(int serialNumber, String originalTitle, String originalDescription, String newTitle, String newDescription) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, newTitle);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, newDescription);

        db.update(DatabaseHelper.TABLE_TASKS, values, DatabaseHelper.COLUMN_TITLE + "=?", new String[]{originalTitle});

        // Update the tasksList with the new task information
        for (int i = 0; i < tasksList.size(); i++) {
            if (tasksList.get(i).startsWith(serialNumber + ". ")) {
                String updatedTaskInfo = serialNumber + ". " + newTitle;
                if (!newDescription.isEmpty()) {
                    updatedTaskInfo += ": " + newDescription;
                }
                tasksList.set(i, updatedTaskInfo);
                break;
            }
        }

        db.close();
    }


    private void deleteTask(int position) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        // Extract the serial number and task title
        String taskInfo = tasksList.get(position);
        String[] taskInfoParts = taskInfo.split("\\. ", 2);
        if (taskInfoParts.length == 2) {
            int serialNumber = Integer.parseInt(taskInfoParts[0]);
            String taskTitle = taskInfoParts[1].split(":")[0].trim();

            // Delete task from the database
            db.delete(DatabaseHelper.TABLE_TASKS, DatabaseHelper.COLUMN_TITLE + "=?", new String[]{taskTitle});

            // Remove the task from the tasksList
            tasksList.remove(position);

            // Refresh the ListView
            tasksAdapter.notifyDataSetChanged();

            if (tasksList.isEmpty()) {
                taskListView.setVisibility(View.GONE);
                tvNoTasks.setVisibility(View.VISIBLE);
            } else {
                taskListView.setVisibility(View.VISIBLE);
                tvNoTasks.setVisibility(View.GONE);
            }
        }

        db.close();
    }


    private void insertTask(String title, String description) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, description);
        db.insert(DatabaseHelper.TABLE_TASKS, null, values);
        db.close();
    }


    private void updateTasksList() {
        tasksList.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_TASKS,
                new String[]{DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DESCRIPTION},
                null, null, null, null, null
        );

        int serialNumber = 1; // Initialize the serial number

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskTitle = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE));
            @SuppressLint("Range") String taskDescription = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION));

            // Create a task string with serial number and task details
            String task = serialNumber + ". " + taskTitle;
            if (taskDescription != null && !taskDescription.isEmpty()) {
                task += ": " + taskDescription;
            }

            tasksList.add(task);

            serialNumber++; // Increment the serial number
        }
        cursor.close();
        db.close();
        tasksAdapter.notifyDataSetChanged();

        if (tasksList.isEmpty()) {
            taskListView.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            taskListView.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);
        }
    }

}