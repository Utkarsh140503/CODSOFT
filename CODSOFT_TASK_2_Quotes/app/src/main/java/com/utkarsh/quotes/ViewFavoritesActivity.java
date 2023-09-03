package com.utkarsh.quotes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ViewFavoritesActivity extends AppCompatActivity {

    private class FavoritesAdapter extends ArrayAdapter<String> {
        private LayoutInflater inflater;

        public FavoritesAdapter(Context context, List<String> objects) {
            super(context, -1, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_item_favorite, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);
            String item = getItem(position);
            textView.setText(item);

            return convertView;
        }
    }

    private ListView favoritesListView;
    private ArrayAdapter<String> adapter;
    private List<String> favoriteQuotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favorites);

        favoritesListView = findViewById(R.id.favoritesListView);

        favoriteQuotes = loadFavoriteQuotes();
        adapter = new FavoritesAdapter(this, favoriteQuotes);
        favoritesListView.setAdapter(adapter);

        updateListViewVisibility(); // Check and update visibility initially

        favoritesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(position);
            }
        });
    }

    private List<String> loadFavoriteQuotes() {
        List<String> favoriteQuotes = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_QUOTE,
                DatabaseHelper.COLUMN_AUTHOR,
                DatabaseHelper.COLUMN_TIMESTAMP
        };

        Cursor cursor = db.query(DatabaseHelper.FAVORITES_TABLE, projection, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String quote = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUOTE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));

            String formattedQuote = "\"" + quote + "\"\n- " + author + "\n* Added: " + timestamp + " *";
            favoriteQuotes.add(formattedQuote);
        }

        cursor.close();
        db.close();
        return favoriteQuotes;
    }

    private void updateListViewVisibility() {
        if (favoriteQuotes.isEmpty()) {
            favoritesListView.setVisibility(View.GONE);
            findViewById(R.id.emptyList).setVisibility(View.VISIBLE);
        } else {
            favoritesListView.setVisibility(View.VISIBLE);
            findViewById(R.id.emptyList).setVisibility(View.GONE);
        }
    }

    private void showDeleteDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this quote?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteQuote(position);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteQuote(int position) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String quoteToDelete = favoriteQuotes.get(position);
        String[] parts = quoteToDelete.split("\n");
        String quote = parts[0].substring(1, parts[0].length() - 1); // Removing the surrounding quotes
        String timestamp = parts[2].substring("* Added: ".length(), parts[2].length() - " *".length());

        int deletedRows = db.delete(DatabaseHelper.FAVORITES_TABLE,
                DatabaseHelper.COLUMN_QUOTE + " = ? AND " + DatabaseHelper.COLUMN_TIMESTAMP + " = ?",
                new String[]{quote, timestamp});

        if (deletedRows > 0) {
            favoriteQuotes.remove(position);
            adapter.notifyDataSetChanged();
            updateListViewVisibility();
        }
        db.close();
    }
}
