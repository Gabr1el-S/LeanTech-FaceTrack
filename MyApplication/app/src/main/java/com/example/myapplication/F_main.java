package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class F_main extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;

    String Stud_Id = "2022-040063";

    String name, str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fmain2);

        // Apply window insets to the main view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the button and set its click listener
        Button button = findViewById(R.id.RecordBTN);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(F_main.this, F_recordsSCRN.class);
            startActivity(intent);
        });
    }

    public void calendar(View view) {
        Intent intent = new Intent(F_main.this, F_CalendarSCRN.class);
        startActivity(intent);
    }

    public void attendance(View view) {
        Intent intent = new Intent(F_main.this, f_attendance.class);
        startActivity(intent);
    }

    public void logout(View view) {
        Intent intent = new Intent(F_main.this, loginpage.class);
        startActivity(intent);
    }





}
