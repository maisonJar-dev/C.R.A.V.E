package com.hci.crave_prototype;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.hci.crave_prototype.model.Ride;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LinearLayout bottomSheet;
    private TextView tvLocationName, tvLocationMeta, tvLocationCategory;
    private EditText etSearch;
    private Button btnGo;

    // Timer & Ride UI
    private View timerCard;
    private TextView tvTimer;
    private ImageButton btnPauseResume;
    private View statusIndicator;
    private Button btnStartRoute;

    private final LatLng KELOWNA_CENTER = new LatLng(49.8880, -119.4960);
    private LatLng destinationLatLng;
    private String destinationName = "";

    // Ride Tracking Logic
    private boolean isRiding = false;
    private Ride currentRide;
    private Location lastLocation;
    private List<LatLng> pathPoints = new ArrayList<>();
    private Polyline ridePolyline;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        etSearch           = view.findViewById(R.id.etSearch);
        btnGo              = view.findViewById(R.id.btnGo);
        bottomSheet        = view.findViewById(R.id.bottomSheet);
        tvLocationName     = view.findViewById(R.id.tvLocationName);
        tvLocationMeta     = view.findViewById(R.id.tvLocationMeta);
        tvLocationCategory = view.findViewById(R.id.tvLocationCategory);

        timerCard          = view.findViewById(R.id.timerCard);
        tvTimer            = view.findViewById(R.id.tvTimer);
        btnPauseResume     = view.findViewById(R.id.btnPauseResume);
        statusIndicator    = view.findViewById(R.id.statusIndicator);

        btnStartRoute      = view.findViewById(R.id.btnStartRoute);
        Button btnLogVisit = view.findViewById(R.id.btnLogVisit);
        View btnClose      = view.findViewById(R.id.btnClose);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnGo.setOnClickListener(v -> searchLocation(etSearch.getText().toString().trim()));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        btnClose.setOnClickListener(v -> {
            bottomSheet.setVisibility(View.GONE);
            // If ride was finished or not started, reset button text
            if (!isRiding) {
                btnStartRoute.setText("Start Route");
                btnStartRoute.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#36BDBD")));
            }
        });

        btnStartRoute.setOnClickListener(v -> {
            if (!isRiding) {
                startRide();
                btnStartRoute.setText("End Ride");
                btnStartRoute.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#D9622B")));
            } else {
                stopRide();
                btnStartRoute.setText("Start Route");
                btnStartRoute.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#36BDBD")));
            }
        });

        btnLogVisit.setOnClickListener(v -> showVisitLoggedOverlay());

        btnPauseResume.setOnClickListener(v -> {
            if (currentRide != null) {
                if (currentRide.isPaused()) resumeRide();
                else pauseRide();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KELOWNA_CENTER, 14f));
        mMap.addMarker(new MarkerOptions()
                .position(KELOWNA_CENTER)
                .title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private void startRide() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        isRiding = true;
        currentRide = new Ride();
        lastLocation = null;
        pathPoints.clear();
        
        if (mMap != null) {
            mMap.clear();
            ridePolyline = mMap.addPolyline(new PolylineOptions().width(12).color(0xFF36BDBD));
        }

        timerCard.setVisibility(View.VISIBLE);
        statusIndicator.setBackgroundColor(Color.parseColor("#36BDBD"));
        btnPauseResume.setImageResource(android.R.drawable.ic_media_pause);

        startLocationUpdates();
        startTimer();
    }

    private void pauseRide() {
        if (currentRide != null) {
            currentRide.pause();
            statusIndicator.setBackgroundColor(Color.parseColor("#E8A825"));
            btnPauseResume.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void resumeRide() {
        if (currentRide != null) {
            currentRide.resume();
            statusIndicator.setBackgroundColor(Color.parseColor("#36BDBD"));
            btnPauseResume.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void stopRide() {
        isRiding = false;
        if (currentRide != null) currentRide.end();

        if (locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
        timerHandler.removeCallbacks(timerRunnable);

        timerCard.setVisibility(View.GONE);
        bottomSheet.setVisibility(View.GONE);

        Toast.makeText(requireContext(), "Ride saved!", Toast.LENGTH_SHORT).show();
    }

    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateDistanceMeters(2f)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (currentRide == null || currentRide.isPaused()) return;

                Location newLocation = result.getLastLocation();
                if (newLocation == null) return;

                LatLng currentLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                pathPoints.add(currentLatLng);
                
                if (ridePolyline != null) ridePolyline.setPoints(pathPoints);
                if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));

                lastLocation = newLocation;
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, null);
        }
    }

    private void startTimer() {
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

    private void searchLocation(String query) {
        if (query.isEmpty() || mMap == null) return;

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

        executorService.execute(() -> {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(query + ", Kelowna, BC", 1);
                mainHandler.post(() -> {
                    if (addresses == null || addresses.isEmpty()) {
                        useFallback(query);
                    } else {
                        Address address = addresses.get(0);
                        destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                        destinationName = address.getFeatureName() != null ? address.getFeatureName() : query;
                        showResultOnMap(destinationName);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> useFallback(query));
            }
        });
    }

    private void useFallback(String query) {
        destinationLatLng = new LatLng(49.8845, -119.4975); 
        destinationName = query + " (Demo Location)";
        showResultOnMap(destinationName);
    }

    private void showResultOnMap(String name) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(KELOWNA_CENTER).title("You")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions()
                .position(destinationLatLng).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        
        mMap.addPolyline(new PolylineOptions()
                .add(KELOWNA_CENTER, destinationLatLng)
                .width(10f).color(0xFF36BDBD));
        
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 14f));

        float dist = distanceBetween(KELOWNA_CENTER, destinationLatLng);
        int mins = (int)(dist / 15 * 60);

        tvLocationName.setText(name);
        tvLocationMeta.setText(String.format("⭐ 4.0/5   🚲 %d min", mins));
        tvLocationCategory.setText(String.format("📍 %.1f km away", dist));
        
        // Reset button state when showing new result
        btnStartRoute.setText("Start Route");
        btnStartRoute.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#36BDBD")));
        
        bottomSheet.setVisibility(View.VISIBLE);
    }

    private void showVisitLoggedOverlay() {
        bottomSheet.setVisibility(View.GONE);
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("⭐ Visit Logged!")
                .setMessage("Great ride! Your stop at " + destinationName +
                        " has been recorded.")
                .setPositiveButton("Back to Map", (d, w) -> d.dismiss())
                .show();
    }

    private float distanceBetween(LatLng a, LatLng b) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0] / 1000f;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}