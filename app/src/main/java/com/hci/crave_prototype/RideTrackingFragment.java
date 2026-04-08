package com.hci.crave_prototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.hci.crave_prototype.model.Ride;

public class RideTrackingFragment extends Fragment {

    private TextView tvStatus, tvTimer, tvDistance, tvSpeed;
    private Button btnStartStop;

    private boolean isRiding = false;
    private Ride currentRide;
    private Location lastLocation;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride_tracking, container, false);

        tvStatus   = view.findViewById(R.id.tvStatus);
        tvTimer    = view.findViewById(R.id.tvTimer);
        tvDistance = view.findViewById(R.id.tvDistance);
        tvSpeed    = view.findViewById(R.id.tvSpeed);
        btnStartStop = view.findViewById(R.id.btnStartStop);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnStartStop.setOnClickListener(v -> {
            if (!isRiding) startRide();
            else stopRide();
        });

        return view;
    }

    private void startRide() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        isRiding = true;
        currentRide = new Ride();
        lastLocation = null;

        tvStatus.setText("Ride in Progress");
        btnStartStop.setText("Stop Ride");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336")));

        // Start GPS updates
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateDistanceMeters(5f)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location newLocation = result.getLastLocation();
                if (newLocation == null) return;

                if (lastLocation != null) {
                    float distanceDelta = lastLocation.distanceTo(newLocation);
                    currentRide.addDistance(distanceDelta);
                    float speedKmh = newLocation.getSpeed() * 3.6f;
                    tvDistance.setText(String.format("%.2f km", currentRide.getDistanceKm()));
                    tvSpeed.setText(String.format("%.1f km/h", speedKmh));
                }
                lastLocation = newLocation;
            }
        };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, null);

        // Start timer
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRiding && currentRide != null) {
                    tvTimer.setText(currentRide.getFormattedTime());
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopRide() {
        isRiding = false;
        currentRide.end();

        fusedLocationClient.removeLocationUpdates(locationCallback);
        timerHandler.removeCallbacks(timerRunnable);

        tvStatus.setText("Ride Complete!");
        btnStartStop.setText("Start Ride");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));

        Toast.makeText(requireContext(),
                String.format("Ride saved! %.2f km in %s",
                        currentRide.getDistanceKm(),
                        currentRide.getFormattedTime()),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isRiding && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        timerHandler.removeCallbacks(timerRunnable);
    }
}