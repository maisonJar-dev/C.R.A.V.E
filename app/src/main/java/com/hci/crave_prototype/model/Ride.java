package com.hci.crave_prototype.model;

public class Ride {
    private long startTimeMillis;
    private long endTimeMillis;
    private float distanceMeters;
    private float averageSpeedKmh;

    public Ride() {
        this.startTimeMillis = System.currentTimeMillis();
        this.distanceMeters = 0f;
    }

    public void end() {
        this.endTimeMillis = System.currentTimeMillis();
    }

    public long getElapsedSeconds() {
        long end = endTimeMillis > 0 ? endTimeMillis : System.currentTimeMillis();
        return (end - startTimeMillis) / 1000;
    }

    public String getFormattedTime() {
        long secs = getElapsedSeconds();
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }

    public float getDistanceKm() {
        return distanceMeters / 1000f;
    }

    public void addDistance(float meters) {
        this.distanceMeters += meters;
    }

    public float getDistanceMeters() { return distanceMeters; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }
}