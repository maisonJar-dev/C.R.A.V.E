package com.hci.crave_prototype.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Visit {
    private String locationName;
    private String category;
    private String notes;
    private double latitude;
    private double longitude;
    private long timestampMillis;

    public Visit(String locationName, String category, String notes, double latitude, double longitude) {
        this.locationName = locationName;
        this.category = category;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestampMillis = System.currentTimeMillis();
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestampMillis));
    }

    public String getLocationName() { return locationName; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public long getTimestampMillis() { return timestampMillis; }
}