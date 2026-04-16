package com.example.maplistpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class EventsListFragment extends Fragment {

    /**
     * Fragment that displays a list of dance events.
     * It handles event filtering via Chips and navigation to event details.
     */
    private static final String TAG = "EventsListFragment";

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private DatabaseHelper dbHelper;
    private ChipGroup chipGroupFilters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_events);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);

        // Init DatabaseHelper
        dbHelper = new DatabaseHelper(requireContext());

        // Configure RecyclerView
        setupRecyclerView();

        // Configure filters
        setupFilters();

        // Initial Data Load
        loadAllEvents();

        return view;
    }

    /**
     * Sets up the RecyclerView with a LinearLayoutManager and the EventsAdapter.
     * Defines the click behavior: navigating to EventDetailActivity.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new EventsAdapter(new java.util.ArrayList<>(), event -> {
            // Cuando se hace click en un evento, abrir EventDetailActivity
            Intent intent = new Intent(requireContext(), EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    /**
     * Configures the ChipGroup listener to filter events based on the user's selection.
     */
    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                loadAllEvents();
                return;
            }

            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chip_all) {
                loadAllEvents();
            } else if (checkedId == R.id.chip_free) {
                loadFreeEvents();

            } else if (checkedId == R.id.chip_standing) {
                loadStandingEvents();
            } else if (checkedId == R.id.chip_seated) {
                loadSeatingEvents();
            } else if (checkedId == R.id.chip_mixed) {
                loadMixedEvents();
            }
        });
    }

    // --- Database Loading Methods ---
    private void loadAllEvents() {
        Log.d(TAG, "Loading all events");
        List<Event> events = dbHelper.getAllEvents();
        updateEventsList(events, "All events");
    }

    private void loadFreeEvents() {
        Log.d(TAG, "Loading free events");
        List<Event> events = dbHelper.getFreeEvents();
        updateEventsList(events, "Free events");
    }

    private void loadStandingEvents() {
        Log.d(TAG, "Loading standing events");
        List<Event> events = dbHelper.getStandingEvents();
        updateEventsList(events, "Standing events");
    }

    private void loadSeatingEvents() {
        Log.d(TAG, "Loading seating events");
        List<Event> events = dbHelper.getSeatingEvents();
        updateEventsList(events, "Seating events");
    }

    private void loadMixedEvents() {
        Log.d(TAG, "Loading mixed events");
        List<Event> events = dbHelper.getMixedEvents();
        updateEventsList(events, "Mixed events");
    }

    private void updateEventsList(List<Event> events, String filterName) {
        if (adapter != null) {
            adapter.updateEvents(events);

            String message = events.size() + " " + filterName + " found";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

            Log.d(TAG, message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up database connection when the fragment is destroyed
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}