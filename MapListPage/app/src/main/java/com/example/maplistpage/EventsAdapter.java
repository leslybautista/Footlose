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
import androidx.annotation.NonNull;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventClickListener listener;

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventsAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

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

// Si es null, mostrar un valor por defecto o vacío
            if (mobilityLevel == null) {
                mobilityLevel = ""; // o "N/A" si quieres un placeholder
            }

            tvMobilityLevel.setText(mobilityLevel);
            tvMobilityLevel.setVisibility(View.VISIBLE); // siempre visible


            itemView.setOnClickListener(v -> listener.onEventClick(event));
        }

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