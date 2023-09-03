package com.example.vtop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentRegistrationActivity extends AppCompatActivity {

    private EditText editTextRegNumber, editTextName, editTextProctor, editTextProctorEID, editTextPassword;
    private Button btnSubmit;
    private TextView loginRedirect, textViewGoToHomePage;

    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Student_Registrations");

        editTextRegNumber = findViewById(R.id.editTextRegNumber);
        editTextName = findViewById(R.id.editTextName);
        editTextProctor = findViewById(R.id.editTextProctor);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextProctorEID = findViewById(R.id.editTextProctorEID);
        btnSubmit = findViewById(R.id.btnSubmit);
        loginRedirect = findViewById(R.id.textViewAlreadyRegistered);
        textViewGoToHomePage = findViewById(R.id.textViewGoToHomePage);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToLogin();
            }
        });

        textViewGoToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHomePage();
            }
        });
    }

    private void registerUser() {
        String regNumber = editTextRegNumber.getText().toString().trim().toUpperCase();
        String name = editTextName.getText().toString().trim().toUpperCase();
        String proctor = editTextProctor.getText().toString().trim().toUpperCase();
        String password = editTextPassword.getText().toString().trim();
        String proctorEID = editTextProctorEID.getText().toString();

        if (!regNumber.isEmpty() && !name.isEmpty() && !proctor.isEmpty() && !proctorEID.isEmpty() && !password.isEmpty()) {
            // Check if the proctor ID is valid
            DatabaseReference employeeReference = FirebaseDatabase.getInstance().getReference("Employee_Registrations").child(proctorEID);
            employeeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Proctor ID is valid, proceed with registration
                        Registration registration = new Registration(regNumber, name, proctor, proctorEID, password);
                        databaseReference.child(regNumber).setValue(registration);
                        redirectToLogin();
                    } else {
                        Toast.makeText(StudentRegistrationActivity.this, "Invalid Proctor ID", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(StudentRegistrationActivity.this, "Error checking Proctor ID", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Fill All The Details!", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(StudentRegistrationActivity.this, StudentLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHomePage() {
        Intent intent = new Intent(StudentRegistrationActivity.this, StartupActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    // Registration data model
    private static class Registration {
        private String proctor;
        private String regNumber;
        private String name;
        private String proctorEID;
        private String password;

        public Registration() {
            // Default constructor required for calls to DataSnapshot.getValue(Registration.class)
        }

        public Registration(String regNumber, String name, String proctor, String proctorEID, String password) {
            this.proctor = proctor;
            this.regNumber = regNumber;
            this.name = name;
            this.proctorEID = proctorEID;
            this.password = password;
        }

        public String getProctor() {
            return proctor;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public String getProctorEID() { return proctorEID; }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }
    }
}
