package com.hci.crave_prototype;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.hci.crave_prototype.leaderboard_helpers.Leaderboard_Model;
import com.hci.crave_prototype.leaderboard_helpers.User_Model;

import java.util.PriorityQueue;

public class LeaderboardFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.leaderboard_view, container, false);

        PriorityQueue<User_Model> lbRanking = getQueue();
        if (lbRanking == null || lbRanking.isEmpty()) {
            return view;
        }

        View overlay = view.findViewById(R.id.p1Info);
        LinearLayout top3 = view.findViewById(R.id.top3List);
        User_Model p1 = lbRanking.poll();
        User_Model p2 = lbRanking.poll();
        User_Model p3 = lbRanking.poll();
        addRow(top3, R.layout.leaderboad_box_first, R.layout.sample_leaderboard_stat_card, p1, 1, overlay);
        addRow(top3, R.layout.leaderboad_box_second, R.layout.sample_leaderboard_stat_card, p2, 2, overlay);
        addRow(top3, R.layout.leaderboad_box_third, R.layout.sample_leaderboard_stat_card, p3, 3, overlay);

        int rank = 4;
        LinearLayout normal = view.findViewById(R.id.leaderboardNormal);
        while (!lbRanking.isEmpty()) {
            User_Model user = lbRanking.poll();
            addRow(normal, R.layout.leaderboad_box_general, R.layout.sample_leaderboard_stat_card, user, rank, overlay);
            rank++;
        }

        return view;
    }

    private void addRow(LinearLayout list, int placementLayout, int cardLayout, User_Model user, int rank, View overlay) {
        View row = getLayoutInflater().inflate(R.layout.leaderboard_row, list, false);

        FrameLayout placementSlot = row.findViewById(R.id.placementSlot);
        FrameLayout cardSlot = row.findViewById(R.id.cardSlot);

        View placement = getLayoutInflater().inflate(placementLayout, placementSlot, false);
        TextView placementText = placement.findViewById(R.id.placementText);
        if (placementText != null) {
            placementText.setText("#" + rank);
        }

        View card = getLayoutInflater().inflate(cardLayout, cardSlot, false);
        bindCard(card, user, rank);

        card.setOnClickListener(click -> {
            if (user == null) return;

            Leaderboard_Model.Leaderboard_Heap.fetchUser(user.getUsername(), fetchedUser -> {
                if (fetchedUser == null || !isAdded()) return;

                Leaderboard_Model.Leaderboard_Heap.updateMap(fetchedUser);
                bindCard(card, fetchedUser, rank);

                ProfileFragment profileFragment = new ProfileFragment();

                // Kyle opens his own editable profile (no Bundle)
                if (!fetchedUser.getUsername().equalsIgnoreCase("kyle")) {
                    Bundle args = new Bundle();
                    args.putString("username", fetchedUser.getUsername());
                    args.putString("name", fetchedUser.getName());
                    args.putInt("dist", fetchedUser.getDist());
                    args.putInt("visits", fetchedUser.getVisits());
                    args.putString("imageName", fetchedUser.getImageName());
                    profileFragment.setArguments(args);
                }

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToProfileTab();
                }
                loadFragment(profileFragment);
            });
        });

        placementSlot.addView(placement);
        cardSlot.addView(card);
        list.addView(row);
    }

    private void bindCard(View card, User_Model user, int rank) {
        if (user == null) return;
        ImageView image = card.findViewById(R.id.imageHolder);
        TextView name = card.findViewById(R.id.name);
        TextView distTxt = card.findViewById(R.id.distTxt);
        TextView visitsTxt = card.findViewById(R.id.visitsTxt);

        String imageResource = user.getImageName();
        int resId = 0;
        if (imageResource != null && !imageResource.isEmpty()) {
            resId = getResources().getIdentifier(
                    imageResource,
                    "drawable",
                    requireContext().getPackageName()
            );
        }
        if (image != null && resId != 0) {
            image.setImageResource(resId);
        }
        if (name != null) name.setText(user.getName());
        if (distTxt != null) distTxt.setText(String.valueOf(user.getDist()));
        if (visitsTxt != null) visitsTxt.setText(String.valueOf(user.getVisits()));
    }

    private PriorityQueue<User_Model> getQueue() {
        return Leaderboard_Model.Leaderboard_Heap.getLeaderboard();
    }

    private void loadFragment(Fragment fragment) {
        if (!isAdded()) return;
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}