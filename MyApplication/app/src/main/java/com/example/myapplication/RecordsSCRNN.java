package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecordsSCRNN extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;
    ResultSet rs;
    String name, str;
    String Stud_Id = "2022-040063";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_records_scrnn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        connectionclass = new Connectionclass();
        connect();

        TextView recordTextView = findViewById(R.id.Recordforday);
        // Get current date
        Date currentDate = new Date();

        // Format the date as needed
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        String formattedDate = dateFormat.format(currentDate);

        // Set text for TextView
        recordTextView.setText("student log for " +"\n"+ formattedDate);


    }



    public void back(View view) {
        Intent intent = new Intent(RecordsSCRNN.this, MainActivity.class);
        startActivity(intent);
    }
    public void btnClick(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();

                String query ="SELECT * FROM attendance_db.logs WHERE student_id = ? AND date =? ORDER BY time DESC";

                PreparedStatement stmt = con.prepareStatement(query);
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                stmt.setString(1, Stud_Id);
                stmt.setString(2,formattedDate);
                ResultSet rs = stmt.executeQuery();
                StringBuilder bStr = new StringBuilder(" ");
                while (rs.next()){
                    bStr.append(rs.getString("first_name")).append(" ").append(rs.getString("activity")).append(" ").append("at").append(" ").append(rs.getString("time")).append("\n").append("\n");

                }
                name = bStr.toString();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(() -> {
                TextView txtlist = findViewById(R.id.RECORDLIST);
                txtlist.setText(name);
                TextView recordTextView = findViewById(R.id.Recordforday);
                txtlist.setMovementMethod(new ScrollingMovementMethod());


            });
        });
    }

    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();

                String query ="SELECT * FROM attendance_db.logs WHERE student_id = ? AND date =? ORDER BY time DESC";

                PreparedStatement stmt = con.prepareStatement(query);
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                stmt.setString(1, Stud_Id);
                stmt.setString(2,formattedDate);
                ResultSet rs = stmt.executeQuery();
                StringBuilder bStr = new StringBuilder(" ");
                while (rs.next()){
                    bStr.append(rs.getString("first_name")).append(" ").append(rs.getString("activity")).append(" ").append("at").append(" ").append(rs.getString("time")).append("\n").append("\n");

                }
                name = bStr.toString();




                if (con == null) {
                    str = "Error in connecting with the database";
                } else {
                    str = "Connected successfully";
                }
            } catch (Exception e) {
                str = "Exception: " + e.getMessage();
            }

            runOnUiThread(() -> {
                Toast.makeText(RecordsSCRNN.this, str, Toast.LENGTH_SHORT).show();
                TextView txtlist = findViewById(R.id.RECORDLIST);
                txtlist.setText(name);
                txtlist.setMovementMethod(new ScrollingMovementMethod());
            });
        });
    }
}