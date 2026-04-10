package com.hci.crave_prototype;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hci.crave_prototype.leaderboard_helpers.Leaderboard_Model;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Load database in background to prevent ANR (App Not Responding)
        Executors.newSingleThreadExecutor().execute(() -> {
            Leaderboard_Model.Leaderboard_Heap.populateDatabase();
        });

        bottomNav = findViewById(R.id.bottom_nav);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_ride) {
                loadFragment(new FeedPageFragment());
            } else if (id == R.id.nav_discover) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.nav_leaderboard) {
                loadFragment(new LoadingFragment());
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            }
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_discover);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void switchToRideTab() {
        bottomNav.setSelectedItemId(R.id.nav_ride);
    }

    public void switchToLeaderboardTab() {
        bottomNav.setSelectedItemId(R.id.nav_leaderboard);
    }

    public void switchToProfileTab() {
        bottomNav.setSelectedItemId(R.id.nav_profile);
    }

    public void switchToDiscoverTab() {
        bottomNav.setSelectedItemId(R.id.nav_discover);
    }
}