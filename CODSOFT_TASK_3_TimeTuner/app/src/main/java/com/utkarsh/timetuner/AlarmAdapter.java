package com.utkarsh.timetuner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AlarmAdapter extends ArrayAdapter<AlarmItem> {
    public AlarmAdapter(Context context, ArrayList<AlarmItem> alarmList) {
        super(context, 0, alarmList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AlarmItem alarmItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_alarm, parent, false);
        }

        TextView alarmTimeTextView = convertView.findViewById(R.id.alarmTimeTextView);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Set the alarm time text
        alarmTimeTextView.setText(alarmItem.getFormattedTime());

        // Handle delete button click
        deleteButton.setOnClickListener(v -> {
            // Stop the alarm sound
            if (getContext() instanceof MainActivity) {
                ((MainActivity) getContext()).stopAlarm();
            }

            // Remove the alarm item from the list and update the ListView
            remove(alarmItem);
            notifyDataSetChanged();
        });

        return convertView;
    }
}
