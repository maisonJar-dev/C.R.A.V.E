package com.hci.crave_prototype.leaderboard_helpers;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hci.crave_prototype.LeaderboardFragment;
import com.hci.crave_prototype.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Leaderboard_Model {
    public static final Leaderboard_Heap LBH = new Leaderboard_Heap();

    public interface UserFetchCallback {
        void onFetched(User_Model user);
    }

    public static class Leaderboard_Heap extends Fragment implements Comparator<User_Model> {
        private static final DatabaseReference craveData = FirebaseDatabase.getInstance().getReference();
        private static PriorityQueue<User_Model> leaderboard = new PriorityQueue<>(new Leaderboard_Heap());
        private static List<User_Model> userList = new ArrayList<>(10);
        public static boolean proceed = false;

        /**
         * To be called onCreate of MainActivity. Populates Database
         */
        public static void populateDatabase() {
            ArrayList<User_Model> temp = new ArrayList<>(10);
            temp.add(new User_Model(50, 5, "Maison G", "mais", "avatar2"));
            temp.add(new User_Model(25, 1, "James P", "james", "avatar3"));
            temp.add(new User_Model(22, 10, "Sheey K", "sheey123", "avatar4"));
            temp.add(new User_Model(80, 12, "Eliz P", "eliz", "avatar5"));
            temp.add(new User_Model(90, 1, "Couche P", "couch", "avatar10"));
            temp.add(new User_Model(120, 8, "Kyle K", "kyle", "avatar1")); // Our User
            temp.add(new User_Model(34, 1, "Greg G", "grog", "avatar6"));
            temp.add(new User_Model(45, 12, "Eric Z", "erpr", "avatar7"));
            temp.add(new User_Model(32, 3, "Jacke L", "lols", "avatar8"));
            temp.add(new User_Model(110, 4, "Zane H", "llzrip", "avatar9"));

            for (User_Model user : temp) {
                craveData.child("users").child(user.getUsername()).setValue(user);
            }
        }

        public static void populateQueue() {
            Log.i(TAG, "populateQueue() Called");
            DatabaseReference crave = FirebaseDatabase.getInstance().getReference("users");

            // Use singleValueEvent so it fires ONCE, not on every DB change
            crave.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Clear both before repopulating to prevent duplicates
                    userList.clear();
                    leaderboard.clear();

                    for (DataSnapshot sn : snapshot.getChildren()) {
                        User_Model user = sn.getValue(User_Model.class);
                        if (user != null) {
                            userList.add(user);
                            leaderboard.offer(user);
                            Log.i(TAG, user.toString());
                        }
                    }

                    proceed = !userList.isEmpty();
                    Log.d(TAG, proceed ? "~queue updated~" : "~queue empty~");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "populateQueue cancelled: " + error.getMessage());
                }
            });
        }

        /**
         * Fetches a single user from Firebase by username and returns them via callback.
         */
        public static void fetchUser(String username, UserFetchCallback callback) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(username)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User_Model user = snapshot.getValue(User_Model.class);
                            callback.onFetched(user);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "fetchUser cancelled: " + error.getMessage());
                            callback.onFetched(null);
                        }
                    });
        }

        public static PriorityQueue<User_Model> getLeaderboard() {
            return leaderboard;
        }


        // Saves a user's updated data back to Firebase and refreshes the queue.
        public static void saveUser(User_Model updatedUser) {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(updatedUser.getUsername())
                    .setValue(updatedUser)
                    .addOnSuccessListener(unused -> {
                        Log.i(TAG, "saveUser: " + updatedUser.getUsername() + " saved to DB");
                        updateMap(updatedUser);
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "saveUser failed: " + e.getMessage()));
        }

        public static void updateMap(User_Model changedUser) {
            Log.i(TAG, "updateMap() Called");
            PriorityQueue<User_Model> temp = new PriorityQueue<>(new Leaderboard_Heap());
            while (!leaderboard.isEmpty()) {
                User_Model user = leaderboard.poll();
                Log.d(TAG, user.toString());
                if (user.getUsername().equalsIgnoreCase(changedUser.getUsername())) {
                    user = changedUser;
                }
                temp.add(user);
            }
            leaderboard = temp;
        }

        @Override
        public int compare(User_Model o1, User_Model o2) {
            int score1 = o1.getDist() + o1.getVisits();
            int score2 = o2.getDist() + o2.getVisits();

            if (score1 != score2) {
                return Integer.compare(score2, score1);
            }
            // Tie-breaker: higher distance first, then higher visits.
            if (o1.getDist() == o2.getDist()) {
                return Integer.compare(o2.getDist(), o1.getDist());
            }
            return Integer.compare(o2.getVisits(), o1.getVisits());
        }
    }
}