package com.utkarsh.todo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CompletedTasksActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private List<String> completedTasksList;
    private ArrayAdapter<String> completedTasksAdapter;
    private ListView completedTasksListView;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed_tasks);

        databaseHelper = new DatabaseHelper(this);

        completedTasksList = new ArrayList<>();
        completedTasksListView = findViewById(R.id.completedTasksListView);
        completedTasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, completedTasksList);
        completedTasksListView.setAdapter(completedTasksAdapter);

        imageView = findViewById(R.id.emptyCompleteTask);

        loadCompletedTasks();
    }

    private void loadCompletedTasks() {
        completedTasksList.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_COMPLETED_TASKS,
                new String[]{DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DESCRIPTION},
                null, null, null, null, null
        );

        while (cursor.moveToNext()) {
            @SuppressLint("Range") String taskTitle = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TITLE));
            @SuppressLint("Range") String taskDescription = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIPTION));
            String taskInfo = taskTitle + ": " + taskDescription;
            completedTasksList.add(taskInfo);
        }

        if(completedTasksList.isEmpty()){
            imageView.setVisibility(View.VISIBLE);
            completedTasksListView.setVisibility(View.GONE);
        }else{
            imageView.setVisibility(View.GONE);
            completedTasksListView.setVisibility(View.VISIBLE);
        }

        cursor.close();
        db.close();
        completedTasksAdapter.notifyDataSetChanged();
    }
}
