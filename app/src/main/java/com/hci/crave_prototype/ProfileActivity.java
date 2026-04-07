package com.hci.crave_prototype;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.hci.crave_prototype.leaderboard_helpers.Leaderboard_Model;

public class ProfileActivity extends AppCompatActivity {

    private TextView nameText;
    private TextView usernameText;
    private TextView bioText;
    private TextView distanceText;
    private TextView visitsText;
    private TextView achievementTitle;
    private TextView achievementHint;
    private TextView distanceCategoryTitle;
    private TextView visitCategoryTitle;
    private TextView consistencyCategoryTitle;

    private ImageView profileImage;

    private Button editButton;
    private Button settingsButton;

    private Button badgeFirstRide;
    private Button badge10km;
    private Button badge50km;

    private Button badgeFirstVisit;
    private Button badgeFoodie;
    private Button badgeLocalTourist;

    private Button badgeWeekendCyclist;
    private Button badgeConsistentRider;
    private Button badgeEarlyBird;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        Leaderboard_Model.Leaderboard_Heap.populateDatabase();
        Leaderboard_Model.Leaderboard_Heap.populateQueue();
        nameText = findViewById(R.id.nameText);
        usernameText = findViewById(R.id.usernameText);
        bioText = findViewById(R.id.bioText);
        distanceText = findViewById(R.id.distanceText);
        visitsText = findViewById(R.id.visitsText);
        achievementTitle = findViewById(R.id.achievementTitle);
        achievementHint = findViewById(R.id.achievementHint);
        distanceCategoryTitle = findViewById(R.id.distanceCategoryTitle);
        visitCategoryTitle = findViewById(R.id.visitCategoryTitle);
        consistencyCategoryTitle = findViewById(R.id.consistencyCategoryTitle);

        profileImage = findViewById(R.id.profileImage);

        editButton = findViewById(R.id.editButton);
        settingsButton = findViewById(R.id.settingsButton);

        badgeFirstRide = findViewById(R.id.badgeFirstRide);
        badge10km = findViewById(R.id.badge10km);
        badge50km = findViewById(R.id.badge50km);

        badgeFirstVisit = findViewById(R.id.badgeFirstVisit);
        badgeFoodie = findViewById(R.id.badgeFoodie);
        badgeLocalTourist = findViewById(R.id.badgeLocalTourist);

        badgeWeekendCyclist = findViewById(R.id.badgeWeekendCyclist);
        badgeConsistentRider = findViewById(R.id.badgeConsistentRider);
        badgeEarlyBird = findViewById(R.id.badgeEarlyBird);

        loadProfileData();

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        badgeFirstRide.setOnClickListener(v -> showAchievementDialog(
                "First Ride",
                "Unlocked after completing your first recorded ride."
        ));

        badge10km.setOnClickListener(v -> showAchievementDialog(
                "10 KM Completed",
                "Unlocked after reaching a total cycling distance of 10 km."
        ));

        badge50km.setOnClickListener(v -> showAchievementDialog(
                "50 KM Completed",
                "Unlocked after reaching a total cycling distance of 50 km."
        ));

        badgeFirstVisit.setOnClickListener(v -> showAchievementDialog(
                "First Visit",
                "Unlocked after visiting your first venue."
        ));

        badgeFoodie.setOnClickListener(v -> showAchievementDialog(
                "Foodie",
                "Unlocked after visiting 5 or more restaurants."
        ));

        badgeLocalTourist.setOnClickListener(v -> showAchievementDialog(
                "Local Tourist",
                "Unlocked after visiting 10 or more local venues."
        ));

        badgeWeekendCyclist.setOnClickListener(v -> showAchievementDialog(
                "Weekend Cyclist",
                "Unlocked by riding on Saturdays, Sundays, or both."
        ));

        badgeConsistentRider.setOnClickListener(v -> showAchievementDialog(
                "Consistent Rider",
                "Unlocked after riding 3 days in a row."
        ));

        badgeEarlyBird.setOnClickListener(v -> showAchievementDialog(
                "Early Bird",
                "Unlocked by riding between 6 AM and 10 AM."
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);

        String fullName = prefs.getString("fullName", "Kyle K.");
        String username = prefs.getString("username", "kyle");
        String bio = prefs.getString("bio", "Exploring Kelowna on wheels");
        String distance = prefs.getString("distance", "120 km");
        String visits = prefs.getString("visits", "8 visits");
        int avatar = prefs.getInt("avatar", R.drawable.avatar1);
        boolean showAchievements = prefs.getBoolean("showAchievements", true);

        nameText.setText(fullName);
        usernameText.setText("@" + username);
        bioText.setText(bio);
        distanceText.setText(distance);
        visitsText.setText(visits);
        profileImage.setImageResource(avatar);

        int achievementVisibility = showAchievements ? View.VISIBLE : View.GONE;

        achievementTitle.setVisibility(achievementVisibility);
        achievementHint.setVisibility(achievementVisibility);

        distanceCategoryTitle.setVisibility(achievementVisibility);
        visitCategoryTitle.setVisibility(achievementVisibility);
        consistencyCategoryTitle.setVisibility(achievementVisibility);

        badgeFirstRide.setVisibility(achievementVisibility);
        badge10km.setVisibility(achievementVisibility);
        badge50km.setVisibility(achievementVisibility);

        badgeFirstVisit.setVisibility(achievementVisibility);
        badgeFoodie.setVisibility(achievementVisibility);
        badgeLocalTourist.setVisibility(achievementVisibility);

        badgeWeekendCyclist.setVisibility(achievementVisibility);
        badgeConsistentRider.setVisibility(achievementVisibility);
        badgeEarlyBird.setVisibility(achievementVisibility);
    }

    private void showAchievementDialog(String title, String description) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }
}