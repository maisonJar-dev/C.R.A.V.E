package com.hci.crave_prototype.model;

public class Ride {
    private long startTimeMillis;
    private long endTimeMillis;
    private long pauseStartTimeMillis;
    private long accumulatedTimeMillis;
    private boolean isPaused;
    private float distanceMeters;

    public Ride() {
        this.startTimeMillis = System.currentTimeMillis();
        this.distanceMeters = 0f;
        this.accumulatedTimeMillis = 0;
        this.isPaused = false;
    }

    public void pause() {
        if (!isPaused) {
            pauseStartTimeMillis = System.currentTimeMillis();
            isPaused = true;
        }
    }

    public void resume() {
        if (isPaused) {
            accumulatedTimeMillis += (System.currentTimeMillis() - pauseStartTimeMillis);
            isPaused = false;
        }
    }

    public void end() {
        if (isPaused) {
            resume();
        }
        this.endTimeMillis = System.currentTimeMillis();
    }

    public long getElapsedSeconds() {
        long currentTotal;
        if (endTimeMillis > 0) {
            currentTotal = (endTimeMillis - startTimeMillis) - accumulatedTimeMillis;
        } else if (isPaused) {
            currentTotal = (pauseStartTimeMillis - startTimeMillis) - accumulatedTimeMillis;
        } else {
            currentTotal = (System.currentTimeMillis() - startTimeMillis) - accumulatedTimeMillis;
        }
        return Math.max(0, currentTotal / 1000);
    }

    public String getFormattedTime() {
        long secs = getElapsedSeconds();
        long hours = secs / 3600;
        long mins = (secs % 3600) / 60;
        long s = secs % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, mins, s);
        } else {
            return String.format("%02d:%02d", mins, s);
        }
    }

    public float getDistanceKm() {
        return distanceMeters / 1000f;
    }

    public void addDistance(float meters) {
        if (!isPaused) {
            this.distanceMeters += meters;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public float getDistanceMeters() { return distanceMeters; }
    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }
}