package com.hci.crave_prototype.leaderboard_helpers;

import static android.content.ContentValues.TAG;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Leaderboard_Model  {
    public static final Leaderboard_Heap LBH = new Leaderboard_Heap();
    public static class Leaderboard_Heap implements Comparator<User_Model> {
        private static final DatabaseReference craveData = FirebaseDatabase.getInstance().getReference();
        private static PriorityQueue<User_Model> leaderboard = new PriorityQueue<>(new Leaderboard_Heap());
        private static List<User_Model> userList = new ArrayList<>(10);

        /**
         * To be called onCreate of MainActivity. Populates Database
         */
        public static void populateDatabase() {
            ArrayList<User_Model> temp = new ArrayList<>(10);
            temp.add(new User_Model(50, 5, "Maison G", "mais"));
            temp.add(new User_Model(25, 1, "James P", "james"));
            temp.add(new User_Model(22, 10, "Sheey K", "sheey123"));
            temp.add(new User_Model(80, 12, "Eliz P", "eliz"));
            temp.add(new User_Model(90, 1, "Couche P", "couch"));
            temp.add(new User_Model(50, 5, "Kyle K", "kyle")); // Our User
            temp.add(new User_Model(34, 1, "Zoe G", "zozo"));
            temp.add(new User_Model(45, 12, "Eric Z", "erpr"));
            temp.add(new User_Model(32, 3, "Lola G", "lols"));
            temp.add(new User_Model(23, 4, "Zane H", "llzrip"));

            for (User_Model user : temp) {
                craveData.child("users").child(user.getUsername()).setValue(user);
            }
        }
        public static void populateQueue() {
            Log.i(TAG, "populateQueue() Called");
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference crave = database.getReference("users");
            try {
                crave.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot sn : snapshot.getChildren()) {
                            User_Model user = sn.getValue(User_Model.class);

                            if (user != null) {
                                userList.add(user);
                                Log.i(TAG, user.toString());
                            }
                        }

                        if (!userList.isEmpty()) {
                            Log.d(TAG, "~updating queue~");
                            for (User_Model user : userList) {
                                leaderboard.offer(user);
                            }
                            Log.d(TAG, "~queue updated~");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        public static void updateMap(User_Model changedUser) {
            Log.i(TAG, "updateMap() Called");
            PriorityQueue<User_Model> temp = new PriorityQueue<>();
            while (!leaderboard.isEmpty()) {
                User_Model user = leaderboard.poll();
                Log.d(TAG, user.toString());
                String un = user.getUsername();
                if (un.equalsIgnoreCase(changedUser.getUsername())) {
                    user = changedUser;
                }
                temp.add(user);
            }
            leaderboard = temp;
        }

        private int hashValues(int dist, int visits) {
            double temp =  (double) dist / visits;
            return (int)temp*100;
        }


        @Override
        public int compare(User_Model o1, User_Model o2) {
            double nValue = hashValues(o1.getDist(), o1.getVisits());
            double xValue = hashValues(o2.getDist(), o2.getVisits());

            return Double.compare(xValue, nValue);
        }
    }
}
