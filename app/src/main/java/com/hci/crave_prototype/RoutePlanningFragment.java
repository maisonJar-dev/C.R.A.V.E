package com.hci.crave_prototype;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
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

public class RoutePlanningFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText etDestination;
    private Button btnSearch, btnSelectRoute;
    private LinearLayout routeInfoPanel;
    private TextView tvDestinationName, tvRouteDistance;

    // Kelowna city center as default start point
    private final LatLng KELOWNA_CENTER = new LatLng(49.8880, -119.4960);
    private LatLng destinationLatLng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_planning, container, false);

        etDestination  = view.findViewById(R.id.etDestination);
        btnSearch      = view.findViewById(R.id.btnSearch);
        routeInfoPanel = view.findViewById(R.id.routeInfoPanel);
        tvDestinationName = view.findViewById(R.id.tvDestinationName);
        tvRouteDistance   = view.findViewById(R.id.tvRouteDistance);
        btnSelectRoute    = view.findViewById(R.id.btnSelectRoute);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnSearch.setOnClickListener(v -> searchDestination());

        etDestination.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDestination();
                return true;
            }
            return false;
        });

        btnSelectRoute.setOnClickListener(v -> {
            Toast.makeText(requireContext(),
                    "Route selected! Starting navigation...",
                    Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(KELOWNA_CENTER, 13f));
        mMap.addMarker(new MarkerOptions()
                .position(KELOWNA_CENTER)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void searchDestination() {
        String query = etDestination.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a destination", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append Kelowna to bias results locally
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query + ", Kelowna, BC", 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            destinationLatLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Clear old markers/routes
            mMap.clear();

            // Add start marker
            mMap.addMarker(new MarkerOptions()
                    .position(KELOWNA_CENTER)
                    .title("Your Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            // Add destination marker
            mMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title(address.getFeatureName()));

            // Draw straight line route
            mMap.addPolyline(new PolylineOptions()
                    .add(KELOWNA_CENTER, destinationLatLng)
                    .width(8f)
                    .color(0xFF4CAF50));

            // Zoom to show both points
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 14f));

            // Show route info panel
            float distanceKm = distanceBetween(KELOWNA_CENTER, destinationLatLng);
            tvDestinationName.setText(address.getFeatureName() != null
                    ? address.getFeatureName() : query);
            tvRouteDistance.setText(String.format("%.1f km away", distanceKm));
            routeInfoPanel.setVisibility(View.VISIBLE);

        } catch (IOException e) {
            Toast.makeText(requireContext(), "Search failed, try again", Toast.LENGTH_SHORT).show();
        }
    }

    private float distanceBetween(LatLng a, LatLng b) {
        float[] result = new float[1];
        android.location.Location.distanceBetween(
                a.latitude, a.longitude,
                b.latitude, b.longitude,
                result);
        return result[0] / 1000f;
    }
}