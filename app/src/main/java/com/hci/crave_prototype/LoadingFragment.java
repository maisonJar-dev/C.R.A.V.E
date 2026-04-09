package com.hci.crave_prototype;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.hci.crave_prototype.leaderboard_helpers.Leaderboard_Model;

public class LoadingFragment extends Fragment {
    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded()) return;
            if (Leaderboard_Model.Leaderboard_Heap.proceed) {
                loadFragment(new LeaderboardFragment());
            } else {
                handler.postDelayed(this, 250);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loader_view, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Leaderboard_Model.Leaderboard_Heap.proceed = false;
        Leaderboard_Model.Leaderboard_Heap.populateQueue();
        handler.postDelayed(pollRunnable, 250);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(pollRunnable);
    }

    private void loadFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
