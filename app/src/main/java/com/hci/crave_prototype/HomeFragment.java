package com.hci.crave_prototype;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LinearLayout bottomSheet;
    private TextView tvLocationName, tvLocationMeta, tvLocationCategory;
    private EditText etSearch;
    private Button btnGo;

    private final LatLng KELOWNA_CENTER = new LatLng(49.8880, -119.4960);
    private LatLng destinationLatLng;
    private String destinationName = "";

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

        Button btnStartRoute = view.findViewById(R.id.btnStartRoute);
        Button btnLogVisit   = view.findViewById(R.id.btnLogVisit);
        View btnClose        = view.findViewById(R.id.btnClose);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnGo.setOnClickListener(v -> searchLocation(etSearch.getText().toString().trim()));

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        btnClose.setOnClickListener(v -> bottomSheet.setVisibility(View.GONE));

        btnStartRoute.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToRideTab();
            }
        });

        btnLogVisit.setOnClickListener(v -> showVisitLoggedOverlay());

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
        
        // ENABLE ZOOM CONTROLS AND GESTURES
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
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