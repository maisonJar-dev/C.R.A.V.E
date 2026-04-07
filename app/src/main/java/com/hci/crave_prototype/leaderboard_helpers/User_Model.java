package com.hci.crave_prototype.leaderboard_helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User_Model {
    private int dist;
    private int visits;
    private String name;
    private String username;
    private static DatabaseReference craveDatabase = FirebaseDatabase.getInstance().getReference();

    //Should not be called on, needed for Firebase
    public User_Model() {

    }

    public User_Model(int dist, int visits, String name, String username) {
        createUser(dist, visits, name, username);
    }

    public void createUser(int dist, int visits, String name, String username) {
        this.dist = dist;
        this.visits = visits;
        this.name = name;
        this.username = username;
    }

    public int getDist() {return dist;}
    public int getVisits() {return visits;}
    public String getName() {return name;}
    public String getUsername() {return username;}


    public void updateDist(int newDist) {
        dist=newDist;
        craveDatabase.child("users").child(getUsername()).child("dist").setValue(newDist);
        //Update the Max Heap (PriorityQueue) due to change
        updateLeaderboard();
    }
    public void updateVisits(int newVisits) {
        visits=newVisits;
        craveDatabase.child("users").child(getUsername()).child("visits").setValue(newVisits);
        //Update the Max Heap (PriorityQueue) due to change
        updateLeaderboard();
    }
    public void updateLeaderboard() {
        //Updates the Heap and re-orders it
        Leaderboard_Model.Leaderboard_Heap.updateMap(this);
    }

    public String toString() {
        return String.format("[Dist: %d, Visits: %d, Name: %s, Username: %s]",getDist(),getVisits(),getName(),getUsername());
    }

}
