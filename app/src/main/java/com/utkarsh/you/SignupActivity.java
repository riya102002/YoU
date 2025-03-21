package com.utkarsh.you;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextName, editTextUsername, editTextEmail, editTextPassword;
    private View btnSignUp;
    private TextView loginRedirect;
    private ImageView googleSignInButton, adminLoginIcon;
    private GoogleSignInClient googleSignInClient; // Add GoogleSignInClient

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    private boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, go to Dashboard directly
            String userId = sharedPreferences.getString("userId", "");
            String name = sharedPreferences.getString("name", "");
            String username = sharedPreferences.getString("username", "");
            sendToDashboard(userId, name, username);
        }

        // Initialize UI elements
        editTextName = findViewById(R.id.editTextNameSignup);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmailSignup);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSignUp = findViewById(R.id.viewWithGradientSignup);
        loginRedirect = findViewById(R.id.loginRedirectText);
        googleSignInButton = findViewById(R.id.imageView1); // Replace with the ID of your Google ImageView
        googleSignInButton.setOnClickListener(view -> signInWithGoogle());

        adminLoginIcon = findViewById(R.id.adminLoginIcon);
        adminLoginIcon.setOnClickListener(view -> {
            Intent i = new Intent(SignupActivity.this, AdminLogin.class);
            startActivity(i);
            finish();
        });

        btnSignUp.setOnClickListener(v -> signUp());

        loginRedirect.setOnClickListener(view -> {
            Intent i = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Save user information to Realtime Database
                            saveUserInfoToDatabase(user.getUid(), user.getDisplayName(), user.getDisplayName(), user.getEmail());

                            // Save login status to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("userId", user.getUid());
                            editor.putString("name", user.getDisplayName());
                            editor.putString("username", user.getDisplayName());
                            editor.apply();

                            Toast.makeText(SignupActivity.this, "Google Sign In successful!", Toast.LENGTH_SHORT).show();
                            sendToDashboard(user.getUid(), user.getDisplayName(), user.getDisplayName());
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignupActivity.this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signUp() {
        final String name = editTextName.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Check if any field is empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User creation successful, update user profile and save additional data to database
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();
                            user.updateProfile(profileUpdates);

                            // Save user information to Realtime Database
                            saveUserInfoToDatabase(user.getUid(), name, username, email);

                            // Save login status to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("userId", user.getUid());
                            editor.putString("name", name);
                            editor.putString("username", username);
                            editor.apply();

                            Toast.makeText(SignupActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                            sendToDashboard(user.getUid(), name, username);
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserInfoToDatabase(String userId, String name, String username, String email) {
        User user = new User(name, username, email);
        databaseReference.child(userId).setValue(user);
    }

    private void sendToDashboard(String userId, String name, String username) {
        Intent intent = new Intent(SignupActivity.this, DashboardActivity.class);
        intent.putExtra("ID", userId);
        intent.putExtra("Name", name);
        intent.putExtra("Username", username);
        startActivity(intent);
        finish();
    }
}

class User {
    private String name;
    private String username;
    private String email;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String name, String username, String email) {
        this.name = name;
        this.username = username;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
