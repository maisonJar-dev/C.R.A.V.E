package com.hci.crave_prototype;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout top3 = findViewById(R.id.top3List);
        addRow(top3, R.layout.leaderboad_box_first, R.layout.sample_leaderboard_stat_card);
        addRow(top3, R.layout.leaderboad_box_second, R.layout.sample_leaderboard_stat_card);
        addRow(top3, R.layout.leaderboad_box_third, R.layout.sample_leaderboard_stat_card);

    }

    private void addRow(LinearLayout list, int placementLayout, int cardLayout) {
        View row = getLayoutInflater().inflate(R.layout.leaderboard_row,list,false);

        FrameLayout placementSlot = row.findViewById(R.id.placementSlot);
        FrameLayout cardSlot = row.findViewById(R.id.cardSlot);

        View placement = getLayoutInflater().inflate(placementLayout, placementSlot, false);
        View card = getLayoutInflater().inflate(cardLayout, cardSlot, false);

        placementSlot.addView(placement);
        cardSlot.addView(card);
        list.addView(row);
    }
}