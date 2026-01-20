package com.example.maplistpage;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Dorito Imports!
import android.app.AlertDialog;
import android.widget.ImageButton;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;


public class MapFragment extends Fragment implements OnMapReadyCallback {

    // ════════════════════════════════════════════════════════
    // SECTION 1: CLASS VARIABLES
    // ════════════════════════════════════════════════════════

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private DatabaseHelper dbHelper;
    private GoogleMap mMap;
    private Button plotButton;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean locationFetched = false;
    private double latitude;
    private double longitude;

    // NEW: Map to connect markers with events
    private Map<Marker, Event> markerEventMap = new HashMap<>();
    private List<Event> allEvents = new ArrayList<>();
    private Marker selectedMarker = null; // Track currently selected marker

    // NEW: Variable to track current zoom level
    private float currentZoom = 10f;

    // ════════════════════════════════════════════════════════
    // SECTION 2: MAP CALLBACK
    // ════════════════════════════════════════════════════════

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if (latitude != 0.0 && longitude != 0.0) {
                LatLng latlng = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 10));
            }

            plotEventsOnMap();

            if (hasLocationPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }

            // NEW: Listener for zoom changes to resize markers
            mMap.setOnCameraIdleListener(() -> {
                float newZoom = mMap.getCameraPosition().zoom;

                // Only update if zoom changed significantly (by 1 or more)
                if (Math.abs(newZoom - currentZoom) >= 1.0f) {
                    currentZoom = newZoom;
                    updateMarkerSizes();
                }
            });

            // NEW: Listener for when a marker is clicked
            mMap.setOnMarkerClickListener(marker -> {

                Event clickedEvent = markerEventMap.get(marker);

                if (clickedEvent != null) {

                    // Reset previous
                    if (selectedMarker != null) {
                        selectedMarker.setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    }

                    // Highlight
                    marker.setIcon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    selectedMarker = marker;

                    marker.showInfoWindow();

                    // 🔹 NEW: scroll list instead of filtering
                    scrollListToEvent(clickedEvent);
                }

                return true;
            });

        }
    };

    // ════════════════════════════════════════════════════════
    // SECTION 3: CREATE VIEW
    // ════════════════════════════════════════════════════════

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        dbHelper = new DatabaseHelper(getContext());
        recyclerView = view.findViewById(R.id.recycler_view_events);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get all events from database
        allEvents = dbHelper.getAllEvents();

        // NEW: Listener for when an event in the list is clicked
        adapter = new EventsAdapter(allEvents, event -> {
            // Instead of opening a new activity, move the map to that event
            moveMapToEvent(event);
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    // ════════════════════════════════════════════════════════
    // SECTION 4: AFTER VIEW IS CREATED
    // ════════════════════════════════════════════════════════

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        dbHelper = new DatabaseHelper(getContext());

        plotButton = view.findViewById(R.id.map_button);

        SupportMapFragment mapFragments =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragments.getMapAsync((OnMapReadyCallback) this);

        plotButton.setOnClickListener(v -> plotEventsOnMap());

        requestLocationUpdates();

        //Dorito --> Connections w/ xml
        LottieAnimationView doritoButton =
                view.findViewById(R.id.dorito_button);

        TextView helpLabel =
                view.findViewById(R.id.help_label);

        // Create Listener
        View.OnClickListener helpClickListener = v -> {
            doritoButton.playAnimation();  // play animation
            showHelpText();                // really open the pop-up
        };

        // Dorito Click
        doritoButton.setOnClickListener(helpClickListener);

        // Text Click
        helpLabel.setOnClickListener(v -> doritoButton.performClick());
    }

    // ════════════════════════════════════════════════════════
    // SECTION 5: CUSTOM METHODS
    // ════════════════════════════════════════════════════════

    /**
     * NEW METHOD: Updates marker sizes based on zoom level
     */
    private void updateMarkerSizes() {
        if (mMap == null) return;

        // Calculate marker size based on zoom level
        // Zoom levels typically range from 2-21
        // At low zoom (far away): smaller markers
        // At high zoom (close up): bigger markers
        float markerSize;

        if (currentZoom < 8) {
            markerSize = 0.5f; // Small
        } else if (currentZoom < 12) {
            markerSize = 0.7f; // Medium
        } else if (currentZoom < 15) {
            markerSize = 1.0f; // Normal (default)
        } else {
            markerSize = 1.3f; // Large (easier to click for seniors)
        }

        // Update all markers
        for (Marker marker : markerEventMap.keySet()) {
            if (marker.equals(selectedMarker)) {
                // Selected marker stays blue/azure
                marker.setIcon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            } else {
                // Default markers stay ORANGE
                marker.setIcon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            }
            marker.setAlpha(0.95f);
        }
    }

    /**
     * MODIFIED METHOD: Moves the map camera to the selected event and highlights it
     */
    private void moveMapToEvent(Event event) {
        if (mMap == null) {
            Toast.makeText(getContext(), "Map not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the marker for this event
        Marker targetMarker = null;
        for (Map.Entry<Marker, Event> entry : markerEventMap.entrySet()) {
            Event e = entry.getValue();

            if (e.getLatitude() == event.getLatitude() &&
                    e.getLongitude() == event.getLongitude()) {

                targetMarker = entry.getKey();
                break;
            }
        }


        // Reset previous selected marker to ORANGE
        if (selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        }

        // Highlight the new marker in blue/azure
        if (targetMarker != null) {
            targetMarker.setIcon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            selectedMarker = targetMarker;
            targetMarker.showInfoWindow();
        }

        // Create event location
        LatLng eventLocation = new LatLng(event.getLatitude(), event.getLongitude());

        // Animate camera to that point with zoom (zoom 15 for close up view)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLocation, 15f));

        // Show toast message
        Toast.makeText(getContext(),
                "Moving to: " + event.getTitle(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * NEW METHOD: Filters the list to scroll to the one event clicked in the map
     */
    private void scrollListToEvent(Event event) {
        for (int i = 0; i < allEvents.size(); i++) {
            Event e = allEvents.get(i);

            if (e.getLatitude() == event.getLatitude() &&
                    e.getLongitude() == event.getLongitude()) {

                recyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }



    /**
     * METHOD: Plots all events on the map
     */
    private void plotEventsOnMap() {

        if (mMap == null) return;

        // Clear previous markers
        mMap.clear();
        markerEventMap.clear();
        selectedMarker = null; // Reset selected marker

        List<Event> events = dbHelper.getAllEvents();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        for (Event e : events) {

            double lat = e.getLatitude();
            double lng = e.getLongitude();

            LatLng position = new LatLng(lat, lng);

            // MODIFIED: Create markers with ORANGE color by default
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(e.getTitle())
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_ORANGE)) // ORANGE by default
                    .alpha(0.95f)
                    .snippet(e.getVenueName()));

            // NEW: Associate marker with event
            markerEventMap.put(marker, e);

            bounds.include(position);
        }

        // Zoom to show all markers
        if (!events.isEmpty()) {
            LatLngBounds b = bounds.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(b, 120));
        }

        // NEW: Restore full list when plotting all events
        adapter = new EventsAdapter(allEvents, event -> {
            moveMapToEvent(event);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Empty - we use the callback above
    }

    //Dorito
    private void showHelpText() {
        LayoutInflater inflater = LayoutInflater.from(getContext()); //open pop-up
        View dialogView = inflater.inflate(R.layout.dialog_help, null); //what should show

        TextView txtHelp = dialogView.findViewById(R.id.txt_help); // connection title
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close); // connection close button

        String helpText =
                "Explore dance events easily:<br><br>"
                        + "• Tap an <b>event in the list</b> to see it on the map.<br>"
                        + "• Tap a <b>map marker</b> for event details.<br>"
                        + "• Use <b>MAP ALL EVENTS NEAR ME</b> to see everything.";


        txtHelp.setText(android.text.Html.fromHtml(helpText));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

    }



    // ════════════════════════════════════════════════════════
    // SECTION 6: GET USER LOCATION
    // ════════════════════════════════════════════════════════

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {

        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                if (locationFetched) return;
                locationFetched = true;

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                if (mMap != null) {
                    LatLng me = new LatLng(latitude, longitude);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 15f));
                }

                if (locationManager != null) {
                    locationManager.removeUpdates(this);
                }
            }
        };

        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            Boolean fineLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                            Boolean coarseLocationGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                            if (fineLocationGranted != null && fineLocationGranted) {

                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10,
                                        locationListener);

                                if (mMap != null) {
                                    mMap.setMyLocationEnabled(true);
                                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                }

                                locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        10000,
                                        10,
                                        locationListener
                                );
                            } else if (coarseLocationGranted != null &&
                                    coarseLocationGranted) {

                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                                        locationListener);

                                if (mMap != null) {
                                    mMap.setMyLocationEnabled(true);
                                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                                }

                                locationManager.requestLocationUpdates(
                                        LocationManager.NETWORK_PROVIDER,
                                        0,
                                        0,
                                        locationListener
                                );
                            } else {
                                Toast.makeText(getContext(), "Location cannot be obtained due to missing permission.", Toast.LENGTH_LONG).show();
                            }
                        });

        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        locationPermissionRequest.launch(PERMISSIONS);
    }

    private boolean hasLocationPermission() {
        return requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }
}