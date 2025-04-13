package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    // Firebase instances
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // UI components
    private TextView welcomeMessage;
    private TextView userEmail;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        setupGoogleSignIn();
        initializeUI();
        displayUserInformation();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setupGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        } catch (Exception e) {
            Log.e(TAG, "Google Sign-In configuration error: " + e.getMessage());
            showToast("Authentication configuration error");
            redirectToLogin();
        }
    }

    private void initializeUI() {
        welcomeMessage = findViewById(R.id.welcomeMessage);
        userEmail = findViewById(R.id.userEmail);
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> performSignOut());
    }

    private void displayUserInformation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            setWelcomeMessage(currentUser);
            setUserEmail(currentUser);
        } else {
            redirectToLogin();
        }
    }

    private void setWelcomeMessage(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            welcomeMessage.setText(String.format("Welcome, %s!", displayName));
        } else {
            welcomeMessage.setText("Welcome!");
        }
    }

    private void setUserEmail(FirebaseUser user) {
        String email = user.getEmail();
        if (email != null && !email.isEmpty()) {
            userEmail.setText(email);
        } else {
            userEmail.setText("No email available");
        }
    }

    private void performSignOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                redirectToLogin();
            } else {
                Log.w(TAG, "Google sign out failed", task.getException());
                showToast("Sign out failed. Please try again.");
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Verify user is still logged in
        if (mAuth.getCurrentUser() == null) {
            redirectToLogin();
        }
    }
}