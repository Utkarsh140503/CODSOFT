package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class EmployeeDashboardActivity extends AppCompatActivity {

    private String empID;
    private ListView leaveRequestsListView, attendanceListView;
    private DatabaseReference leaveRequestsRef, attendanceRef, employeeInfoRef;
    private List<String> leaveRequestsList, attendanceList;
    private ArrayAdapter<String> leaveRequestsAdapter, attendanceRequestsAdapter;
    Button sendMessageButton;
//    private StorageReference storageReference;
    private ImageView imgProfile;
//    private TextView employeeNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_dashboard);

        imgProfile = findViewById(R.id.imgProfile);

        Intent intent = getIntent();
        if (intent != null) {
            empID = intent.getStringExtra("ID");
        }
        employeeInfoRef = FirebaseDatabase.getInstance().getReference()
                .child("Employee_Registrations").child(empID);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        // Load employee information
        employeeInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String empName = dataSnapshot.child("name").getValue(String.class);
                    TextView nameTextView = findViewById(R.id.employeeNameTextView);
                    nameTextView.setText(empName);

                    // Retrieving employee profile image
                    String imageName = empID + "_profile_image.jpg"; // Assuming image file name

                    StorageReference imageRef = storageReference.child(imageName);
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(EmployeeDashboardActivity.this)
                                    .load(uri)
                                    .placeholder(R.mipmap.ic_launcher)
                                    .into(imgProfile);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            imgProfile.setImageResource(R.mipmap.ic_launcher);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });

        Button displayButton = findViewById(R.id.displayButton);
        Button displayAttendace = findViewById(R.id.displayAttendanceButton);
        leaveRequestsListView = findViewById(R.id.leaveRequestsListView);
        attendanceListView = findViewById(R.id.attendanceListView);
        sendMessageButton = findViewById(R.id.sendMessageButton);

        leaveRequestsList = new ArrayList<>();
        attendanceList = new ArrayList<>();
        leaveRequestsAdapter = new ArrayAdapter<String>(this, R.layout.list_item_leave_request, R.id.leaveRequestTextView, leaveRequestsList);
        attendanceRequestsAdapter = new ArrayAdapter<String>(this, R.layout.list_item_attendance, R.id.attendanceTextView, attendanceList);
        leaveRequestsListView.setAdapter(leaveRequestsAdapter);
        attendanceListView.setAdapter(attendanceRequestsAdapter);
        leaveRequestsListView.setVisibility(View.GONE); // Initially hide the ListView
        attendanceListView.setVisibility(View.GONE);

        leaveRequestsRef = FirebaseDatabase.getInstance().getReference()
                .child("AllLeaveRequestsByStudents").child(empID);

        attendanceRef = FirebaseDatabase.getInstance().getReference()
                .child("Student_Attendance").child(empID);


        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrieveLeaveRequests();
                leaveRequestsListView.setVisibility(View.VISIBLE); // Show the ListView when the button is pressed
                attendanceListView.setVisibility(View.GONE);
            }
        });

        displayAttendace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retrieveAttendance();
                leaveRequestsListView.setVisibility(View.GONE); // Show the ListView when the button is pressed
                attendanceListView.setVisibility(View.VISIBLE);
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(EmployeeDashboardActivity.this, EmployeeSendMessageActivity.class);
                intent1.putExtra("empID", empID);
                startActivity(intent1);
            }
        });

        leaveRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String userID = leaveRequestsList.get(position);
                openLeaveRequestsDetails(userID);
            }
        });

        attendanceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String userID = attendanceList.get(i);
                openAttendanceDetails(userID);
            }
        });
    }

    private void retrieveAttendance(){
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                attendanceList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userID = userSnapshot.getKey();
                    attendanceList.add(userID);
                }

                attendanceRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void retrieveLeaveRequests() {
        leaveRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leaveRequestsList.clear();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userID = userSnapshot.getKey();
                    leaveRequestsList.add(userID);
                }

                leaveRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void openLeaveRequestsDetails(String userID) {
        Intent intent = new Intent(EmployeeDashboardActivity.this, LeaveRequestsDetailsActivity.class);
        intent.putExtra("empID", empID);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    public void openAttendanceDetails(String userID){
        Intent intent = new Intent(EmployeeDashboardActivity.this, AttendanceDetailsActivity.class);
        intent.putExtra("empID", empID);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
}
