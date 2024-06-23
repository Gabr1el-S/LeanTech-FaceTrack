package com.example.myapplication;

import android.os.Bundle;
import android.widget.CalendarView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarSCRN extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;

    String name, str;
    private CalendarView calendarView;
    private TextView event;
    private String Stud_Id = "2022-040063";
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_scrn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectionclass = new Connectionclass();
        connect();

        calendarView = findViewById(R.id.Calendar);
        event = findViewById(R.id.Recordtext);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year, month, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd yyyy");
            String selectedDate = sdf.format(selectedCalendar.getTime());


            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    con = connectionclass.CONN();
                    String query = "SELECT * FROM attendance_db.masterattendance WHERE student_id = ? AND date = ?";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setString(1, Stud_Id);
                    stmt.setString(2, selectedDate);
                    ResultSet rs = stmt.executeQuery();
                    StringBuilder bStr = new StringBuilder("");
                    if (rs.isBeforeFirst()) { // Check if there are results
                        while (rs.next()) {
                            bStr.append(rs.getString("first_name")).append(" ").append("arrived at").append(" ").append(rs.getString("time")).append("\n");
                        }
                        name = bStr.toString();
                    } else {
                        name = "No records found for the selected date";
                    }

                    runOnUiThread(() -> {
                        event.setText(name);
                    });

                    rs.close();
                    stmt.close();
                    con.close();
                } catch (SQLException e) {
                    runOnUiThread(() -> {
                        event.setText("Error retrieving data: " + e.getMessage());
                    });
                }
            });
        });
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
                Toast.makeText(CalendarSCRN.this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }
}
