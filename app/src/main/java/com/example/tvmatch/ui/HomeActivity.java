package com.example.tvmatch.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tvmatch.R;
import com.example.tvmatch.auth.AuthManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {
    private ImageView profileImageView;
    private EditText searchUserEditText;
    private TextView usernameView;
    private Button myListsButton, exploreButton, changeProfilePictureButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AuthManager authManager;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profileImageView = findViewById(R.id.profileImageView);
        searchUserEditText = findViewById(R.id.searchUserEditText);
        myListsButton = findViewById(R.id.myListsButton);
        exploreButton = findViewById(R.id.exploreButton);
        changeProfilePictureButton = findViewById(R.id.changeProfilePictureButton);
        usernameView=findViewById(R.id.usernameView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        authManager = new AuthManager();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Display user profile picture if available
        loadProfilePicture(currentUser.getUid());

        // Set up listeners for buttons
        myListsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to "My Lists" activity
                //Intent intent = new Intent(HomeActivity.this, MyListsActivity.class);
                //startActivity(intent);
            }
        });

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to "Explore" activity (for browsing movies/TV shows using TMDB API)
                //Intent intent = new Intent(HomeActivity.this, ExploreActivity.class);
                //startActivity(intent);
            }
        });

        changeProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle changing the profile picture (could involve file picker, etc.)
                Toast.makeText(HomeActivity.this, "Change Profile Picture clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Search for other users
        searchUserEditText.setOnEditorActionListener((v, actionId, event) -> {
            String searchQuery = searchUserEditText.getText().toString();
            if (!searchQuery.isEmpty()) {
                // Implement search functionality for other users
                searchForUser(searchQuery);
            } else {
                Toast.makeText(HomeActivity.this, "Enter a username to search", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        FirebaseUser user = mAuth.getCurrentUser();
        db.collection("users").document(user.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String username = document.getString("username");
                                usernameView.setText(username+"'s profile");
                            }
                        }
                    }
                });
    }

    private void searchForUser(String username) {
        db.collection("users").whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Handle the case where the user is found
                        Toast.makeText(HomeActivity.this, "User found: " + username, Toast.LENGTH_SHORT).show();
                        // You can add further action to view the user's profile, etc.
                    } else {
                        // Handle the case where no user is found
                        Toast.makeText(HomeActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadProfilePicture(String uid) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String profileUrl = task.getResult().getString("profilePictureUrl");
                            if (profileUrl != null && !profileUrl.isEmpty()) {
                                Picasso.get().load(profileUrl).into(profileImageView);
                            } else {
                                profileImageView.setImageResource(R.drawable.ic_profile_placeholder); // Default profile image
                            }
                        }
                    }
                });
    }

    private void uploadProfilePicture() {
        if (imageUri != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                StorageReference storageRef = storage.getReference("profile_pictures/" + user.getUid() + ".jpg");

                storageRef.putFile(imageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Get the download URL and store it in Firestore
                                    storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful() && task.getResult() != null) {
                                                String downloadUrl = task.getResult().toString();
                                                saveProfilePictureUrl(user.getUid(), downloadUrl);
                                            } else {
                                                Toast.makeText(HomeActivity.this, "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(HomeActivity.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }
}
