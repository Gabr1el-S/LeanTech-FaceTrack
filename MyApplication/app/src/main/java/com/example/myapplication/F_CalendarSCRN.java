package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class F_CalendarSCRN extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;

    String name, str;
    private CalendarView calendarView;
    private TextView event;
    private String Stud_Id;
    private String selectedDate;
    EditText searchID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcalendar_scrn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectionclass = new Connectionclass();
        connect();
        searchID = findViewById(R.id.searchid);

        calendarView = findViewById(R.id.Calendar);
        event = findViewById(R.id.Recordtext);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            Stud_Id = searchID.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
            String selectedDate = sdf.format(selectedCalendar.getTime());
            Log.e("date: ",selectedDate );
            Log.e("searching: ",Stud_Id );

            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    con = connectionclass.CONN();
                    if (con == null) {
                        runOnUiThread(() -> Toast.makeText(F_CalendarSCRN.this, "Database connection failed", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    if (Stud_Id.isEmpty()) {
                        runOnUiThread(() -> {
                            Toast.makeText(F_CalendarSCRN.this, "Search box is empty", Toast.LENGTH_SHORT).show();
                            event.setText("Please input student ID");
                        });
                    } else {
                        String query = "SELECT * FROM attendance_db.masterattendance WHERE student_id = '"+ Stud_Id+"' AND date = '"+selectedDate+"'";
                        search(query);
                        Log.e("query: ",query);
                    }
                } finally {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        });
    }

    public void search(String query) {
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder bStr = new StringBuilder("");
            if (rs.isBeforeFirst()) { // Check if there are results
                while (rs.next()) {
                    bStr.append(rs.getString("first_name")).append(" arrived at ").append(rs.getString("time")).append("\n");
                }
                runOnUiThread(() -> event.setText(bStr.toString()));
            } else {
                runOnUiThread(() -> event.setText("No records found for the selected date"));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            runOnUiThread(() -> event.setText("Error retrieving data: " + e.getMessage()));
        }
    }



    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();
                if (con == null) {
                    str = "Error in connecting with the database";
                } else {
                    str = "Connected successfully";
                }
            } catch (Exception e) {
                str = "Exception: " + e.getMessage();
            }

            runOnUiThread(() -> {
                Toast.makeText(F_CalendarSCRN.this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
