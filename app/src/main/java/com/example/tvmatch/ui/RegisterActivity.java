package com.example.tvmatch.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tvmatch.R;
import com.example.tvmatch.auth.AuthManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText, emailEditText, passwordEditText, repeatPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameText);
        emailEditText = findViewById(R.id.emailText);
        passwordEditText = findViewById(R.id.insertPassword);
        repeatPassword = findViewById(R.id.repeatPassword);
        registerButton = findViewById(R.id.confirmRegisterButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authManager = new AuthManager();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String repeatedPassword = repeatPassword.getText().toString();

                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || repeatedPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return; // Exit the method if fields are empty
                }

                if(!password.equals(repeatedPassword)){
                    Toast.makeText(RegisterActivity.this, "Passwords do not match. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                registerUser(username, email, password);
            }
        });
    }

    private void registerUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User created successfully, now store the username
                            authManager.signIn(email, password, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success
                                        Toast.makeText(RegisterActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(RegisterActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveUsernameToFirestore(user.getUid(), username);
                        } else {
                            // Registration failed
                            Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUsernameToFirestore(String uid, String username) {
        // Create a user object with the username
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);

        // Save the username in Firestore under a collection called "users"
        db.collection("users").document(uid).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                            // Redirect to login or main activity
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to store username", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
