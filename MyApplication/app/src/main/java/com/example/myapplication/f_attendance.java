package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
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

public class f_attendance extends AppCompatActivity {
    Connectionclass connectionclass;
    Connection con;
    ResultSet rs;
    String name, str;

    EditText searchID;
    EditText searchSEC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fattendance);
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
    }

    public void back(View view) {
        Intent intent = new Intent(f_attendance.this, F_main.class);
        startActivity(intent);
    }
    public void download(View view){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                String tableName = "attendance_db.attendance_"+formattedDate;
                String query;
                String section = searchID.getText().toString().toUpperCase();
                if (section.isEmpty()){
                    query = "SELECT * FROM " + tableName;
                }
                else
                query = "SELECT * FROM " + tableName + " WHERE section = ?";


                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, section);
                Log.e("query: ",query );
                ResultSet rs = stmt.executeQuery();

                // Create a new Excel workbook and sheet
                Workbook workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("Attendance Data");

                // Create the header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Student ID", "Status", "Date"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }

                // Populate the sheet with data
                int rowIndex = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(rs.getString("student_id"));
                    row.createCell(1).setCellValue(rs.getString("status"));
                    row.createCell(2).setCellValue(rs.getString("date"));
                }

                // Write the workbook to a file
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "attendance_data_"+formattedDate+".xlsx");
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                if (con == null) {
                    str = "Error in connecting with the database";
                } else {
                    str = "Data downloaded successfully as Excel file";
                }
            } catch (Exception e) {
                str = "Exception: " + e.getMessage();
            }

            runOnUiThread(() -> {
                Toast.makeText(f_attendance.this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }
    public void btnClick(View view) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                con = connectionclass.CONN();
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                String tableName = "attendance_db.attendance_"+formattedDate;

                String section = searchID.getText().toString().toUpperCase();
                String query;
                if (section.isEmpty()) {
                    query = "SELECT * FROM " + tableName +" ORDER BY time DESC";
                } else if (!section.isEmpty()) {
                    query = "SELECT * FROM " + tableName + " WHERE section = '"+section+ "' ORDER BY time DESC";
                    Log.i("print:", section);

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(f_attendance.this, "Invalid search", Toast.LENGTH_SHORT).show();
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
            bStr.append(rs.getString("first_name")).append(" ").append("arrived").append(" ").append("at").append(" ").append(rs.getString("time")).append("\n").append("\n");
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
                Date currentDate = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd", Locale.US);
                String formattedDate = dateFormat.format(currentDate);
                String query = "SELECT * FROM attendance_db.attendance_"+formattedDate+" ORDER BY time DESC";
                PreparedStatement stmt = con.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();
                Log.i("query: ",query);
                Log.i("query: ",stmt.toString());
                Log.i("query: ",rs.toString());
                StringBuilder bStr = new StringBuilder(" ");
                while (rs.next()) {
                    bStr.append(rs.getString("first_name"))
                            .append(" (")
                            .append(rs.getString("section"))
                            .append(") ")
                            .append(" Arrived ")
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
                Toast.makeText(f_attendance.this, str, Toast.LENGTH_SHORT).show();
                TextView txtlist = findViewById(R.id.RECORDLIST);
                txtlist.setText(name);
                txtlist.setMovementMethod(new ScrollingMovementMethod());
            });
        });
    }
}
