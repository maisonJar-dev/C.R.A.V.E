package com.hci.crave_prototype.leaderboard_helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Leaderboard_Model  {
    public static final Leaderboard_Heap LBH = new Leaderboard_Heap();
    public static class Leaderboard_Heap implements Comparator<User_Model> {
        private static DatabaseReference craveData = FirebaseDatabase.getInstance().getReference();
        private static PriorityQueue<User_Model> leaderboard = new PriorityQueue<>(new Leaderboard_Heap());
        private static ArrayList<User_Model> leaderboardSorted = new ArrayList<>(100);

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
            temp.add(new User_Model(50, 5, "Kyle K", "kyle"));
            temp.add(new User_Model(34, 1, "Zoe G", "zozo"));
            temp.add(new User_Model(45, 12, "Eric Z", "erpr"));
            temp.add(new User_Model(32, 3, "Lola G", "lols"));
            temp.add(new User_Model(23, 4, "Zane H", "llzrip"));

            for (User_Model user : temp) {
                craveData.child("users").child(user.getUserName()).setValue(user);
            }
        }
        private void populateQueue() {

        }
        private void sortQueue() {

        }

        public static void updateHeap(String username) {

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
