package com.example.maplistpage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class EventsListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    private DatabaseHelper dbHelper;
    private ChipGroup chipGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_events_list, container, false);

        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recycler_view_events);
        chipGroup = view.findViewById(R.id.chip_group_filters);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load all events by default
        List<Event> events = dbHelper.getAllEvents();

        Log.d("DB_TEST", "getAllEvents returned: " + events.size() + " events");

        adapter = new EventsAdapter(events, event -> {
            Intent intent = new Intent(getContext(), EventDetailActivity.class);
            intent.putExtra("event_id", event.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupFilters();

        return view;
    }

    private void setupFilters() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    String filterText = chip.getText().toString();
                    filterEvents(filterText);
                }
            }
        });


    }
    private void filterEvents(String filterText) {
        List<Event> filteredEvents;

        switch (filterText.toLowerCase()) {
            case "all":
                filteredEvents = dbHelper.getAllEvents();
                break;

            case "free":
                filteredEvents = dbHelper.getFreeEvents(); // Devuelve is_free = 1
                break;

            case "weelchair accesible": // Fíjate que coincida exactamente con el chip
                filteredEvents = dbHelper.getAccessibleEvents(); // Devuelve wheelchair_accessible = 1
                break;

            default:
                // Si quieres filtrar por event type (otros tipos de eventos)
                filteredEvents = dbHelper.getEventsByType(filterText);
                break;
        }

        Log.d("DB_TEST", "filterEvents('" + filterText + "') returned " + filteredEvents.size() + " events");

        adapter.updateEvents(filteredEvents);
    }
}