package com.utkarsh.you;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AdminLogin extends AppCompatActivity {

    TextView username, password;
    RelativeLayout viewAdminWithGradient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        username = findViewById(R.id.editTextAdminEmailLogin);
        password = findViewById(R.id.editTextAdminPasswordLogin);
        viewAdminWithGradient = findViewById(R.id.viewAdminWithGradient);

        viewAdminWithGradient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().toString().equals("admin@you.com")&&password.getText().toString().equals("admin")){
                    Toast.makeText(getApplicationContext(), "Hey Admin!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(AdminLogin.this, AdminDashboard.class);
                    startActivity(i);
                    finish();
                }
                else{
                    username.setError("Invalid");
                    password.setError("Invalid");
                }
            }
        });

    }
}