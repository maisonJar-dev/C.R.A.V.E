package com.hci.crave_prototype;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

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

    // badges are now LinearLayouts, not Buttons
    private LinearLayout badgeFirstRide;
    private LinearLayout badge10km;
    private LinearLayout badge50km;

    private LinearLayout badgeFirstVisit;
    private LinearLayout badgeFoodie;
    private LinearLayout badgeLocalTourist;

    private LinearLayout badgeWeekendCyclist;
    private LinearLayout badgeConsistentRider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        nameText = view.findViewById(R.id.nameText);
        usernameText = view.findViewById(R.id.usernameText);
        bioText = view.findViewById(R.id.bioText);
        distanceText = view.findViewById(R.id.distanceText);
        visitsText = view.findViewById(R.id.visitsText);

        achievementTitle = view.findViewById(R.id.achievementTitle);
        achievementHint = view.findViewById(R.id.achievementHint);
        distanceCategoryTitle = view.findViewById(R.id.distanceCategoryTitle);
        visitCategoryTitle = view.findViewById(R.id.visitCategoryTitle);
        consistencyCategoryTitle = view.findViewById(R.id.consistencyCategoryTitle);

        profileImage = view.findViewById(R.id.profileImage);

        editButton = view.findViewById(R.id.editButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        badgeFirstRide = view.findViewById(R.id.badgeFirstRide);
        badge10km = view.findViewById(R.id.badge10km);
        badge50km = view.findViewById(R.id.badge50km);

        badgeFirstVisit = view.findViewById(R.id.badgeFirstVisit);
        badgeFoodie = view.findViewById(R.id.badgeFoodie);
        badgeLocalTourist = view.findViewById(R.id.badgeLocalTourist);

        badgeWeekendCyclist = view.findViewById(R.id.badgeWeekendCyclist);
        badgeConsistentRider = view.findViewById(R.id.badgeConsistentRider);

        loadProfileData();

        editButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));

        settingsButton.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

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

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
    }

    private void loadProfileData() {
        if (getContext() == null) {
            return;
        }

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE);

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
    }

    private void showAchievementDialog(String title, String description) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }
}