package com.example.maplistpage;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter responsible for managing and displaying a list of Event objects
 * within a RecyclerView.
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    // Interface to handle click events on individual list items.
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    // Inflate the custom layout for a single event row
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    // Get the data model based on position and bind it to the view
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // Updates the data set and refreshes the UI.
    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    //Inner class to hold and manage the views for each event item.
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvTime, tvVenue, tvPrice, tvMobilityLevel;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvVenue = itemView.findViewById(R.id.tv_event_venue);
            tvPrice = itemView.findViewById(R.id.tv_event_price);
            tvMobilityLevel = itemView.findViewById(R.id.tv_mobility_level);
        }

        void bind(Event event, OnEventClickListener listener) {
            tvTitle.setText(event.getTitle());
            tvDate.setText(formatDate(event.getEventDate()));
            tvTime.setText(event.getStartTime() + " - " + event.getEndTime());
            tvVenue.setText(event.getVenueName());
            tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", event.getPrice()));

            String mobilityLevel = event.getMobilityLevel(); // "Mixed", "Standing", "Seated"

            if (mobilityLevel == null) {
                mobilityLevel = "";
            }

            tvMobilityLevel.setText(mobilityLevel);
            tvMobilityLevel.setVisibility(View.VISIBLE);


            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }

        // Converts date from "yyyy-MM-dd" to a user-friendly "Sun, Jan 22" format.
        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateStr;
            }
        }
    }
}