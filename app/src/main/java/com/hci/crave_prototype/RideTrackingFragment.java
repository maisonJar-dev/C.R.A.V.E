package com.hci.crave_prototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hci.crave_prototype.model.Ride;

import java.util.ArrayList;
import java.util.List;

public class RideTrackingFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvStatus, tvTimer, tvDistance, tvSpeed;
    private Button btnStartStop;
    private View statusIndicator;
    private GoogleMap mMap;

    private boolean isRiding = false;
    private Ride currentRide;
    private Location lastLocation;
    private List<LatLng> pathPoints = new ArrayList<>();
    private Polyline polyline;

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
        statusIndicator = view.findViewById(R.id.statusIndicator);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_ride);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnStartStop.setOnClickListener(v -> {
            if (!isRiding) startRide();
            else stopRide();
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Default to Kelowna
        LatLng kelowna = new LatLng(49.8880, -119.4960);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kelowna, 15f));
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
        pathPoints.clear();
        if (mMap != null) {
            mMap.clear();
            polyline = mMap.addPolyline(new PolylineOptions().width(12).color(0xFF36BDBD)); // Electric Teal
        }

        tvStatus.setText("Ride in Progress");
        btnStartStop.setText("Stop Ride");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#D9622B"))); // Blaze Orange for Stop
        statusIndicator.setBackgroundColor(Color.parseColor("#36BDBD")); // Electric Teal for active

        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateDistanceMeters(2f)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location newLocation = result.getLastLocation();
                if (newLocation == null) return;

                LatLng currentLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                pathPoints.add(currentLatLng);
                
                if (polyline != null) {
                    polyline.setPoints(pathPoints);
                }
                
                if (mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
                }

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

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        timerHandler.removeCallbacks(timerRunnable);

        tvStatus.setText("Ride Complete!");
        btnStartStop.setText("Start Ride");
        btnStartStop.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.parseColor("#36BDBD"))); // Electric Teal for Start
        statusIndicator.setBackgroundColor(Color.GRAY);

        Toast.makeText(requireContext(),
                String.format("Ride saved! %.2f km in %s",
                        currentRide.getDistanceKm(),
                        currentRide.getFormattedTime()),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        timerHandler.removeCallbacks(timerRunnable);
    }
}