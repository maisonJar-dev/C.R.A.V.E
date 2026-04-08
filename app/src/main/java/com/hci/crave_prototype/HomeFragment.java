package com.hci.crave_prototype;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LinearLayout bottomSheet;
    private TextView tvLocationName, tvLocationMeta, tvLocationCategory;
    private android.widget.EditText etSearch;

    private final LatLng KELOWNA_CENTER = new LatLng(49.8880, -119.4960);
    private LatLng destinationLatLng;
    private String destinationName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        etSearch          = view.findViewById(R.id.etSearch);
        bottomSheet       = view.findViewById(R.id.bottomSheet);
        tvLocationName    = view.findViewById(R.id.tvLocationName);
        tvLocationMeta    = view.findViewById(R.id.tvLocationMeta);
        tvLocationCategory = view.findViewById(R.id.tvLocationCategory);

        Button btnStartRoute = view.findViewById(R.id.btnStartRoute);
        Button btnLogVisit   = view.findViewById(R.id.btnLogVisit);
        Button btnClose      = view.findViewById(R.id.btnClose);
        Button btnCall       = view.findViewById(R.id.btnCall);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        btnClose.setOnClickListener(v -> bottomSheet.setVisibility(View.GONE));

        btnStartRoute.setOnClickListener(v -> {
            // Switch to ride tab and pass destination
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToRideTab();
            }
        });

        btnLogVisit.setOnClickListener(v -> {
            // Show visit logged confirmation overlay
            showVisitLoggedOverlay(view);
        });

        btnCall.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Calling " + destinationName + "...", Toast.LENGTH_SHORT).show());

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
    }

    private void searchLocation(String query) {
        if (query.isEmpty()) return;
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query + ", Kelowna, BC", 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Address address = addresses.get(0);
            destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude());
            destinationName = address.getFeatureName() != null ? address.getFeatureName() : query;

            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(KELOWNA_CENTER).title("You")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng).title(destinationName)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            mMap.addPolyline(new PolylineOptions()
                    .add(KELOWNA_CENTER, destinationLatLng)
                    .width(10f).color(0xFF2ABFBF));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 14f));

            float dist = distanceBetween(KELOWNA_CENTER, destinationLatLng);
            int mins = (int)(dist / 15 * 60);

            tvLocationName.setText(destinationName);
            tvLocationMeta.setText(String.format("⭐ 4.0/5   🚲 %d min", mins));
            tvLocationCategory.setText("📍 " + String.format("%.1f km away", dist));
            bottomSheet.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showVisitLoggedOverlay(View rootView) {
        // Dismiss bottom sheet
        bottomSheet.setVisibility(View.GONE);

        // Show teal confirmation card
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setTitle("⭐ Visit Logged!")
                .setMessage("Great ride! Your stop at " + destinationName +
                        " has been recorded and added to your Food Counter. " +
                        "You've earned +10 Exploration Points!")
                .setPositiveButton("Back to Map", (d, w) -> d.dismiss())
                .setNegativeButton("View Leaderboard", (d, w) -> {
                    d.dismiss();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToLeaderboardTab();
                    }
                })
                .create();
        dialog.show();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                .setTextColor(android.graphics.Color.parseColor("#2ABFBF"));
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(android.graphics.Color.parseColor("#E8871E"));
    }

    private float distanceBetween(LatLng a, LatLng b) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0] / 1000f;
    }
}