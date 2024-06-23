package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
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
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class F_recordsSCRN extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;
    ResultSet rs;
    String name, str;

    EditText searchID;
    EditText searchSEC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frecords_scrn);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connectionclass = new Connectionclass();
        connect();

        TextView recordTextView = findViewById(R.id.Recordforday);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        String formattedDate = dateFormat.format(currentDate);
        recordTextView.setText("student log for " + "\n" + formattedDate);

        searchID = findViewById(R.id.searchID);
        searchSEC = findViewById(R.id.searchSEC);
    }

    public void back(View view) {
        Intent intent = new Intent(F_recordsSCRN.this, F_main.class);
        startActivity(intent);
    }

    public void btnClick(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                String tableName = "attendance_db.logs";

                String student_id = searchID.getText().toString();
                String section = searchSEC.getText().toString().toUpperCase();
                String query;
                if (student_id.isEmpty() && section.isEmpty()) {
                    query = "SELECT * FROM " + tableName +" WHERE date = '"+formattedDate+"' ORDER BY time DESC";
                } else if (!student_id.isEmpty()) {
                    query = "SELECT * FROM " + tableName + " WHERE student_id = '"+student_id+ "' AND date = '"+formattedDate+"' ORDER BY time DESC";
                    Log.i("print:", student_id);
                } else if (!section.isEmpty()) {
                    query = "SELECT * FROM " + tableName + " WHERE section = '"+section+"' AND date = '"+formattedDate+"' ORDER BY time DESC";
                    Log.i("print:", section);
                } else {
                    runOnUiThread(() -> {
                    Toast.makeText(F_recordsSCRN.this, "Invalid search", Toast.LENGTH_SHORT).show();
                    });
                    return; // No need to proceed further
                }
                Log.i("date: ",formattedDate);
                Log.i("searching: ", query);
                search(query);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                executorService.shutdown();
            }
        });
    }

    public void search(String query) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(query);
        Log.e("data: ", stmt.toString());
        ResultSet rs = stmt.executeQuery();
        Log.i("rs: ",  rs.toString());
        StringBuilder bStr = new StringBuilder(" ");
        while (rs.next()) {
            bStr.append(rs.getString("first_name")).append(" ").append(rs.getString("activity")).append(" ").append("at").append(" ").append(rs.getString("time")).append("\n").append("\n");
        }
        name = bStr.toString();
        Log.i("FINAL DATA: ", name);

        runOnUiThread(() -> {
            TextView txtlist = findViewById(R.id.RECORDLIST);
            txtlist.setText(name);
            txtlist.setMovementMethod(new ScrollingMovementMethod());
        });
    }

    public void connect() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();

                String query = "SELECT * FROM attendance_db.logs WHERE date =? ORDER BY time DESC";
                PreparedStatement stmt = con.prepareStatement(query);
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd yyyy", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                stmt.setString(1, formattedDate);
                ResultSet rs = stmt.executeQuery();
                Log.i("query: ",query);
                Log.i("query: ",stmt.toString());
                Log.i("query: ",rs.toString());
                StringBuilder bStr = new StringBuilder(" ");
                while (rs.next()) {
                    bStr.append(rs.getString("first_name"))
                            .append(" ")
                            .append(rs.getString("activity"))
                            .append(" at ")
                            .append(rs.getString("time"))
                            .append("\n\n");
                }
                name = bStr.toString();

                str = (con == null) ? "Error in connecting with the database" : "Connected successfully";
            } catch (Exception e) {
                str = "Exception: " + e.getMessage();
            }

            runOnUiThread(() -> {
                Toast.makeText(F_recordsSCRN.this, str, Toast.LENGTH_SHORT).show();
                TextView txtlist = findViewById(R.id.RECORDLIST);
                txtlist.setText(name);
                txtlist.setMovementMethod(new ScrollingMovementMethod());
            });
        });
    }
}
