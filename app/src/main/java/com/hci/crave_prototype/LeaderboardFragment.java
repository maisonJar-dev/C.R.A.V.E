package com.hci.crave_prototype;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

public class LeaderboardFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView tv = new TextView(requireContext());
        tv.setText("🏆 Leaderboard\nComing soon...");
        tv.setTextSize(24f);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(32, 32, 32, 32);
        return tv;
    }
}