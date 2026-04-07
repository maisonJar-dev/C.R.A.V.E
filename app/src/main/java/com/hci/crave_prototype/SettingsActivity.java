package com.hci.crave_prototype;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch notificationsSwitch;
    private Switch showAchievementsSwitch;
    private Button aboutButton;
    private Button helpButton;
    private Button contactButton;
    private Button saveSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        showAchievementsSwitch = findViewById(R.id.showAchievementsSwitch);
        aboutButton = findViewById(R.id.aboutButton);
        helpButton = findViewById(R.id.helpButton);
        contactButton = findViewById(R.id.contactButton);
        saveSettingsButton = findViewById(R.id.saveSettingsButton);

        loadSettings();

        aboutButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("About the App")
                .setMessage("CRAVE is a cycling and venue discovery app for exploring Kelowna.")
                .setPositiveButton("OK", null)
                .show());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("Use Edit Profile to update your profile. Tap achievements to read what they mean. Use Settings to control profile display options.")
                .setPositiveButton("OK", null)
                .show());

        contactButton.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Contact Us")
                .setMessage("Email: cravehelp@demo.com\nPhone: 250-555-1234")
                .setPositiveButton("OK", null)
                .show());

        saveSettingsButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);

        boolean notificationsEnabled = prefs.getBoolean("notificationsEnabled", true);
        boolean showAchievements = prefs.getBoolean("showAchievements", true);

        notificationsSwitch.setChecked(notificationsEnabled);
        showAchievementsSwitch.setChecked(showAchievements);
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean("notificationsEnabled", notificationsSwitch.isChecked());
        editor.putBoolean("showAchievements", showAchievementsSwitch.isChecked());

        editor.apply();

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();

        finish();
    }
}
