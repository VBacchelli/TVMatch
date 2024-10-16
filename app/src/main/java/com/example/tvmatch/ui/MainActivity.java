package com.example.tvmatch.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.example.tvmatch.auth.AuthManager;
import com.example.tvmatch.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private AuthManager authManager;
    private TextView statusTextView;
    private Button loginButton, logoutButton, startButton;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        loginButton=findViewById(R.id.loginButton);
        statusTextView=findViewById(R.id.statusTextView);
        logoutButton=findViewById(R.id.logoutButton);
        startButton=findViewById(R.id.startButton);

        db= FirebaseFirestore.getInstance();

        authManager = new AuthManager();

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    db.collection("users").document(user.getUid()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document.exists()) {
                                            String username = document.getString("username");
                                            statusTextView.setText("Welcome, " + username);
                                            loginButton.setVisibility(View.GONE);// Hide login button if the user is already signed in
                                            startButton.setVisibility(View.VISIBLE);
                                            logoutButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            });

                } else {
                    // User is signed out, but stay on this screen and show login option
                    statusTextView.setText("You are not logged in.");
                    loginButton.setVisibility(View.VISIBLE);
                    logoutButton.setVisibility(View.GONE);
                    startButton.setVisibility(View.GONE);
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
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout Confirmation")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                authManager.signOut();
                            }
                        })
                        .setNegativeButton("No", null) // Dismiss dialog
                        .show();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
