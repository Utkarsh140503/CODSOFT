package com.example.vtop;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class StartupActivity extends AppCompatActivity {

    LinearLayout layout1, layout2, layout3, layout4;
    private ListView listView;
    private String[] itemList = {"Spotlight",
            "HEADING 1",
            "HEADING 2",
            "HEADING 3",
            "HEADING 4",
            "HEADING 5"};


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        layout1 = findViewById(R.id.layout1);
        layout2 = findViewById(R.id.layout2);
        layout3 = findViewById(R.id.layout3);
        layout4 = findViewById(R.id.layout4);

        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, StudentLoginActivity.class);
                startActivity(intent);
            }
        });

        layout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartupActivity.this, EmployeeLoginActivity.class);
                startActivity(intent);
            }
        });

        layout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartupActivity.this, "Parent Login Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        layout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StartupActivity.this, "Alumni Login Coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });

        listView = findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedItem = itemList[position];
                if (selectedItem.equals("HEADING 1")) {
                    String url = "https://drive.google.com/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else if (selectedItem.equals("HEADING 2")) {
                    String url = "https://drive.google.com/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("HEADING 3")) {
                    String url = "https://drive.google.com/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("HEADING 4")) {
                    String url = "https://drive.google.com/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else if (selectedItem.equals("HEADING 5")) {
                    String url = "https://vtopregcc.vit.ac.in/";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } else {
                    Toast.makeText(StartupActivity.this, selectedItem, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}