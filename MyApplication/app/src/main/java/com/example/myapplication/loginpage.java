package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class loginpage extends AppCompatActivity {

    Button inpy;
    EditText passwordInput;
    EditText emailInput;
    TextView textView;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Ensure R.layout.login exists and is correct

        // Find views by ID
        inpy = findViewById(R.id.inpy);
        passwordInput = findViewById(R.id.passwordInput);
        emailInput = findViewById(R.id.emailInput);
        textView = findViewById(R.id.head);
        textView2 = findViewById(R.id.textView2);

        // Handling window insets for immersive UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBarsInsets.left, systemBarsInsets.top, systemBarsInsets.right, systemBarsInsets.bottom);
            return insets;
        });

        // Set onClick listener for button
        inpy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input from email and password fields
                String userEmail = emailInput.getText().toString();
                String userPassword = passwordInput.getText().toString();

                // Validate credentials
                if (userPassword.equals("parentpassword") && userEmail.equals("parent@apc")) {
                    // Navigate to parent main activity
                    Intent intent = new Intent(loginpage.this, MainActivity.class);
                    startActivity(intent);
                } else if (userPassword.equals("facultypassword") && userEmail.equals("faculty@apc")) {
                    // Navigate to faculty main activity
                    Intent intent = new Intent(loginpage.this, F_main.class);
                    startActivity(intent);
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(loginpage.this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                })
           ; }
        }
    });
}
}
