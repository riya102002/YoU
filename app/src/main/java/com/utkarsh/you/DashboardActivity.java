package com.utkarsh.you;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class DashboardActivity extends AppCompatActivity {

    private ChipNavigationBar chipNavigationBar;
    private Fragment selectedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
        }
        setContentView(R.layout.activity_dashboard);

        // Retrieve values from the intent
        String userId = getIntent().getStringExtra("ID");
        String name = getIntent().getStringExtra("Name");
        String username = getIntent().getStringExtra("Username");

        chipNavigationBar = findViewById(R.id.bottomNavigationView);
        chipNavigationBar.setItemSelected(R.id.home, true); // Select Home on startup
        HomeFragment homeFragment = new HomeFragment();
        loadFragment(homeFragment, userId, name, username); // Load HomeFragment on startup

        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                switch (id) {
                    case R.id.home:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
                        }
                        loadFragment(new HomeFragment(), userId, name, username);
                        break;
                    case R.id.activity:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.parseColor("#1b7593"));
                        }
                        loadFragment(new ActivityFragment(), userId, name, username);
                        break;
                    case R.id.settings:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.parseColor("#FCF9C1"));
                        }
                        loadFragment(new SettingsFragment(), userId, name, username);
                        break;
                    case R.id.logout:
                        showLogoutDialog();
                        break;
                }
            }
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("You Will Be Logged Out!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear shared preferences
                        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();

                        // Logout and start LoginActivity
                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        // Finish the current activity
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadFragment(Fragment fragment, String userId, String name, String username) {
        Bundle bundle = new Bundle();
        bundle.putString("ID", userId);
        bundle.putString("Name", name);
        bundle.putString("Username", username);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

}
