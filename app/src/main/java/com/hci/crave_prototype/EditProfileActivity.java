package com.hci.crave_prototype;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText fullNameInput;
    private EditText usernameInput;
    private EditText bioInput;
    private Button saveButton;
    private ImageView avatarImage;

    private int selectedAvatar = R.drawable.avatar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        fullNameInput = findViewById(R.id.fullNameInput);
        usernameInput = findViewById(R.id.usernameInput);
        bioInput = findViewById(R.id.bioInput);
        saveButton = findViewById(R.id.saveButton);
        avatarImage = findViewById(R.id.avatarImage);

        loadExistingData();

        avatarImage.setOnClickListener(v -> showAvatarPicker());

        saveButton.setOnClickListener(v -> validateAndConfirmSave());
    }

    private void loadExistingData() {
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);

        fullNameInput.setText(prefs.getString("fullName", ""));
        usernameInput.setText(prefs.getString("username", ""));
        bioInput.setText(prefs.getString("bio", ""));

        selectedAvatar = prefs.getInt("avatar", R.drawable.avatar1);
        avatarImage.setImageResource(selectedAvatar);
    }

    private void showAvatarPicker() {
        String[] options = {"Avatar 1", "Avatar 2", "Avatar 3", "Avatar 4", "Avatar 5"};

        int[] avatarIds = {
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4,
                R.drawable.avatar5
        };

        new AlertDialog.Builder(this)
                .setTitle("Choose Avatar")
                .setItems(options, (dialog, which) -> {
                    selectedAvatar = avatarIds[which];
                    avatarImage.setImageResource(selectedAvatar);
                })
                .show();
    }

    private void validateAndConfirmSave() {
        ValidationResult result = validateInputs();

        if (!result.isValid) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Save")
                .setMessage("Please review your details before saving.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    ValidationResult finalResult = validateInputs();

                    if (!finalResult.isValid) {
                        return;
                    }

                    saveValidatedProfile(finalResult.fullName, finalResult.username, finalResult.bio);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private ValidationResult validateInputs() {
        String fullName = fullNameInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        fullNameInput.setError(null);
        usernameInput.setError(null);
        bioInput.setError(null);

        boolean isValid = true;

        // Full name checks
        if (fullName.isEmpty()) {
            fullNameInput.setError("Full name is required");
            isValid = false;
        } else if (!fullName.matches("[a-zA-Z ]+")) {
            fullNameInput.setError("Use letters and spaces only");
            isValid = false;
        }

        // Username checks
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            isValid = false;
        } else {
            if (username.contains("@")) {
                usernameInput.setError("Do not type @. It is added automatically");
                isValid = false;
            }
            if (!username.matches("[a-zA-Z0-9-]+")) {
                usernameInput.setError("Use only letters, numbers, and hyphens");
                isValid = false;
            }
            if (username.length() > 8) {
                usernameInput.setError("Maximum 8 characters");
                isValid = false;
            }
        }

        // Bio check
        if (bio.length() > 150) {
            bioInput.setError("Bio must be 150 characters or less");
            isValid = false;
        }

        return new ValidationResult(isValid, fullName, username, bio);
    }

    private void saveValidatedProfile(String fullName, String username, String bio) {
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("fullName", fullName);
        editor.putString("username", username); // save WITHOUT @
        editor.putString("bio", bio);
        editor.putInt("avatar", selectedAvatar);

        editor.apply();

        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private static class ValidationResult {
        boolean isValid;
        String fullName;
        String username;
        String bio;

        ValidationResult(boolean isValid, String fullName, String username, String bio) {
            this.isValid = isValid;
            this.fullName = fullName;
            this.username = username;
            this.bio = bio;
        }
    }
}