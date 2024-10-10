package com.example.tvmatch.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.tvmatch.auth.AuthManager;
import com.example.tvmatch.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private AuthManager authManager;
    private TextView statusTextView;
    private Button loginButton;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        loginButton=findViewById(R.id.loginButton);
        statusTextView=findViewById(R.id.statusTextView);
        logoutButton=findViewById(R.id.logoutButton);


        authManager = new AuthManager();

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    statusTextView.setText("Welcome, " + user.getEmail());
                    loginButton.setVisibility(View.GONE);  // Hide login button if the user is already signed in
                } else {
                    // User is signed out, but stay on this screen and show login option
                    statusTextView.setText("You are not logged in.");
                    loginButton.setVisibility(View.VISIBLE);
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                authManager.signOut();

            }
        });
    }
}
