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

import com.hci.crave_prototype.leaderboard_helpers.User_Model;

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

    private LinearLayout badgeFirstRide;
    private LinearLayout badge10km;
    private LinearLayout badge50km;
    private LinearLayout badgeFirstVisit;
    private LinearLayout badgeFoodie;
    private LinearLayout badgeLocalTourist;
    private LinearLayout badgeWeekendCyclist;
    private LinearLayout badgeConsistentRider;

    // Non-null only when opened from leaderboard for another user
    private User_Model bundleUser = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        nameText                 = view.findViewById(R.id.nameText);
        usernameText             = view.findViewById(R.id.usernameText);
        bioText                  = view.findViewById(R.id.bioText);
        distanceText             = view.findViewById(R.id.distanceText);
        visitsText               = view.findViewById(R.id.visitsText);
        achievementTitle         = view.findViewById(R.id.achievementTitle);
        achievementHint          = view.findViewById(R.id.achievementHint);
        distanceCategoryTitle    = view.findViewById(R.id.distanceCategoryTitle);
        visitCategoryTitle       = view.findViewById(R.id.visitCategoryTitle);
        consistencyCategoryTitle = view.findViewById(R.id.consistencyCategoryTitle);
        profileImage             = view.findViewById(R.id.profileImage);
        editButton               = view.findViewById(R.id.editButton);
        settingsButton           = view.findViewById(R.id.settingsButton);
        badgeFirstRide           = view.findViewById(R.id.badgeFirstRide);
        badge10km                = view.findViewById(R.id.badge10km);
        badge50km                = view.findViewById(R.id.badge50km);
        badgeFirstVisit          = view.findViewById(R.id.badgeFirstVisit);
        badgeFoodie              = view.findViewById(R.id.badgeFoodie);
        badgeLocalTourist        = view.findViewById(R.id.badgeLocalTourist);
        badgeWeekendCyclist      = view.findViewById(R.id.badgeWeekendCyclist);
        badgeConsistentRider     = view.findViewById(R.id.badgeConsistentRider);

        // Check if a leaderboard user was passed in
        Bundle args = getArguments();
        if (args != null && args.containsKey("username")) {
            bundleUser = new User_Model(
                    args.getInt("dist", 0),
                    args.getInt("visits", 0),
                    args.getString("name", ""),
                    args.getString("username", ""),
                    args.getString("imageName", "")
            );
        }

        loadProfileData();

        // Other users: hide edit/settings
        // Kyle (no bundle): keep edit/settings functional as normal
        if (bundleUser != null) {
            editButton.setVisibility(View.GONE);
            settingsButton.setVisibility(View.GONE);
        } else {
            editButton.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), EditProfileActivity.class)));
            settingsButton.setOnClickListener(v ->
                    startActivity(new Intent(requireContext(), SettingsActivity.class)));
        }

        // Badge click listeners — same for all users
        badgeFirstRide.setOnClickListener(v -> showAchievementDialog(
                "First Ride", "Unlocked after completing your first recorded ride."));
        badge10km.setOnClickListener(v -> showAchievementDialog(
                "10 KM Completed", "Unlocked after reaching a total cycling distance of 10 km."));
        badge50km.setOnClickListener(v -> showAchievementDialog(
                "50 KM Completed", "Unlocked after reaching a total cycling distance of 50 km."));
        badgeFirstVisit.setOnClickListener(v -> showAchievementDialog(
                "First Visit", "Unlocked after visiting your first venue."));
        badgeFoodie.setOnClickListener(v -> showAchievementDialog(
                "Foodie", "Unlocked after visiting 5 or more restaurants."));
        badgeLocalTourist.setOnClickListener(v -> showAchievementDialog(
                "Local Tourist", "Unlocked after visiting 10 or more local venues."));
        badgeWeekendCyclist.setOnClickListener(v -> showAchievementDialog(
                "Weekend Cyclist", "Unlocked by riding on Saturdays, Sundays, or both."));
        badgeConsistentRider.setOnClickListener(v -> showAchievementDialog(
                "Consistent Rider", "Unlocked after riding 3 days in a row."));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only reload from SharedPreferences for Kyle's own profile
        if (bundleUser == null) {
            loadProfileData();
        }
    }

    private void loadProfileData() {
        if (getContext() == null) return;

        if (bundleUser != null) {
            // ---- Other leaderboard user ----
            nameText.setText(bundleUser.getName());
            usernameText.setText("@" + bundleUser.getUsername());
            bioText.setText("");
            distanceText.setText(bundleUser.getDist() + " km");
            visitsText.setText(bundleUser.getVisits() + " visits");

            int resId = getResources().getIdentifier(
                    bundleUser.getImageName(), "drawable", requireContext().getPackageName());
            if (resId != 0) profileImage.setImageResource(resId);

            // Show achievements section, unlocked based on their stats
            setAchievementsVisible(true);
            bindAchievements(bundleUser.getDist(), bundleUser.getVisits());

        } else {
            // ---- Kyle's own profile: SharedPreferences, unchanged ----
            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("ProfilePrefs", android.content.Context.MODE_PRIVATE);

            String fullName          = prefs.getString("fullName", "Kyle K.");
            String username          = prefs.getString("username", "kyle");
            String bio               = prefs.getString("bio", "Exploring Kelowna on wheels");
            String distance          = prefs.getString("distance", "120 km");
            String visits            = prefs.getString("visits", "8 visits");
            int avatar               = prefs.getInt("avatar", R.drawable.avatar1);
            boolean showAchievements = prefs.getBoolean("showAchievements", true);

            nameText.setText(fullName);
            usernameText.setText("@" + username);
            bioText.setText(bio);
            distanceText.setText(distance);
            visitsText.setText(visits);
            profileImage.setImageResource(avatar);

            setAchievementsVisible(showAchievements);

            if (showAchievements) {
                // Parse Kyle's distance int out of the "120 km" string for badge unlocking
                int distInt = 0;
                int visitsInt = 0;
                try {
                    distInt   = Integer.parseInt(distance.replaceAll("[^0-9]", ""));
                    visitsInt = Integer.parseInt(visits.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException ignored) {}
                bindAchievements(distInt, visitsInt);
            }
        }
    }

    /**
     * Locks or unlocks each badge based on the user's dist and visits.
     * Unlocked = full opacity. Locked = 30% opacity so users can see what's available.
     */
    private void bindAchievements(int dist, int visits) {
        setBadgeUnlocked(badgeFirstRide,       dist >= 1);
        setBadgeUnlocked(badge10km,            dist >= 10);
        setBadgeUnlocked(badge50km,            dist >= 50);
        setBadgeUnlocked(badgeFirstVisit,      visits >= 1);
        setBadgeUnlocked(badgeFoodie,          visits >= 5);
        setBadgeUnlocked(badgeLocalTourist,    visits >= 10);
        // No ride-day data available yet — always locked for now
        setBadgeUnlocked(badgeWeekendCyclist,  false);
        setBadgeUnlocked(badgeConsistentRider, false);
    }

    private void setBadgeUnlocked(LinearLayout badge, boolean unlocked) {
        if (badge == null) return;
        badge.setAlpha(unlocked ? 1.0f : 0.3f);
    }

    private void setAchievementsVisible(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        achievementTitle.setVisibility(v);
        achievementHint.setVisibility(v);
        distanceCategoryTitle.setVisibility(v);
        visitCategoryTitle.setVisibility(v);
        consistencyCategoryTitle.setVisibility(v);
        badgeFirstRide.setVisibility(v);
        badge10km.setVisibility(v);
        badge50km.setVisibility(v);
        badgeFirstVisit.setVisibility(v);
        badgeFoodie.setVisibility(v);
        badgeLocalTourist.setVisibility(v);
        badgeWeekendCyclist.setVisibility(v);
        badgeConsistentRider.setVisibility(v);
    }

    private void showAchievementDialog(String title, String description) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(description)
                .setPositiveButton("OK", null)
                .show();
    }
}