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

public class MainActivity extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;

    String Stud_Id = "2022-040063";

    String name, str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Apply window insets to the main view
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        connectionclass = new Connectionclass();
        connect();
        // Initialize the button and set its click listener
        Button button = findViewById(R.id.RecordBTN);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecordsSCRNN.class);
            startActivity(intent);
        });
    }

    public void calendar(View view) {
        Intent intent = new Intent(MainActivity.this, CalendarSCRN.class);
        startActivity(intent);
    }

    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();

                // Get the current date and format it for the table name
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
                String tableName = "attendance_db.attendance_" + dateFormat.format(currentDate);

                String query = "SELECT * FROM " + tableName + " WHERE student_id = ?";

                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, Stud_Id);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String status = rs.getString("status");
                    runOnUiThread(() -> updateStatusCircle(status));
                }

                if (con == null) {
                    str = "Error in connecting with the database";
                } else {
                    str = "Connected successfully";
                }
            } catch (Exception e) {
                str = "Exception: " + e.getMessage();
            }

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void updateStatusCircle(String status) {
        ImageView statusCircle = findViewById(R.id.status_circle);
        if (status.equals("PRESENT")) {
            statusCircle.setColorFilter(Color.parseColor("#00FF00"), PorterDuff.Mode.SRC_IN);
        } else if (status.equals("OFF CAMPUS")) {
            statusCircle.setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.SRC_IN); // Red color
        }
    }

    public void faculty(View view) {
        Intent intent = new Intent(MainActivity.this, loginpage.class);
        startActivity(intent);
    }


}
