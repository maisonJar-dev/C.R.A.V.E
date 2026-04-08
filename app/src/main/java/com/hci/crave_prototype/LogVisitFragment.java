package com.hci.crave_prototype;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.hci.crave_prototype.model.Visit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogVisitFragment extends Fragment {

    private TextView tvCurrentLocation;
    private EditText etLocationName, etNotes;
    private RadioGroup radioCategory;
    private Button btnLogVisit;
    private LinearLayout recentVisitsContainer;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLat = 49.8880;
    private double currentLng = -119.4960;

    private List<Visit> visits = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_visit, container, false);

        tvCurrentLocation     = view.findViewById(R.id.tvCurrentLocation);
        etLocationName        = view.findViewById(R.id.etLocationName);
        etNotes               = view.findViewById(R.id.etNotes);
        radioCategory         = view.findViewById(R.id.radioCategory);
        btnLogVisit           = view.findViewById(R.id.btnLogVisit);
        recentVisitsContainer = view.findViewById(R.id.recentVisitsContainer);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        detectLocation();

        btnLogVisit.setOnClickListener(v -> logVisit());

        return view;
    }

    private void detectLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvCurrentLocation.setText("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();

                // Reverse geocode to get address
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(currentLat, currentLng, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        String street = addr.getThoroughfare() != null ? addr.getThoroughfare() : "";
                        String city = addr.getLocality() != null ? addr.getLocality() : "Kelowna";
                        tvCurrentLocation.setText("📍 " + street + ", " + city);
                    }
                } catch (IOException e) {
                    tvCurrentLocation.setText("📍 Kelowna, BC");
                }
            } else {
                tvCurrentLocation.setText("📍 Kelowna, BC (default)");
            }
        });
    }

    private void logVisit() {
        String name = etLocationName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a place name", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = etNotes.getText().toString().trim();

        int selectedId = radioCategory.getCheckedRadioButtonId();
        String category = "Restaurant";
        if (selectedId == R.id.radioCafe) category = "Café";
        else if (selectedId == R.id.radioPark) category = "Park";

        Visit visit = new Visit(name, category, notes, currentLat, currentLng);
        visits.add(visit);

        // Clear inputs
        etLocationName.setText("");
        etNotes.setText("");

        // Show success
        Toast.makeText(requireContext(),
                "✅ Visit logged! Food counter: " + visits.size(),
                Toast.LENGTH_SHORT).show();

        // Check for achievements
        checkAchievements();

        // Update recent visits list
        addVisitCard(visit);
    }

    private void checkAchievements() {
        if (visits.size() == 1) {
            showAchievement("🏅 First Visit badge earned!");
        } else if (visits.size() == 5) {
            showAchievement("🏅 Foodie badge earned!");
        } else if (visits.size() == 10) {
            showAchievement("🏅 Local Tourist badge earned!");
        }
    }

    private void showAchievement(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void addVisitCard(Visit visit) {
        View card = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_2, recentVisitsContainer, false);

        TextView text1 = card.findViewById(android.R.id.text1);
        TextView text2 = card.findViewById(android.R.id.text2);

        text1.setText(visit.getLocationName() + " (" + visit.getCategory() + ")");
        text2.setText(visit.getFormattedDate());

        recentVisitsContainer.addView(card, 0);
    }
}