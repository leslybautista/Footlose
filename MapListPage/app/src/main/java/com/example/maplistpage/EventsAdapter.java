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
        TextView tvEventType, tvTitle, tvDate, tvTime, tvVenue, tvDescription, tvPrice, tvBadge;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventType = itemView.findViewById(R.id.tv_event_type);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvDate = itemView.findViewById(R.id.tv_event_date);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvVenue = itemView.findViewById(R.id.tv_event_venue);
            tvDescription = itemView.findViewById(R.id.tv_event_description);
            tvPrice = itemView.findViewById(R.id.tv_event_price);
            tvBadge = itemView.findViewById(R.id.tv_registration_badge);
        }

        void bind(Event event, OnEventClickListener listener) {
            tvEventType.setText(event.getEventType());
            tvTitle.setText(event.getTitle());
            tvDate.setText(formatDate(event.getEventDate()));
            tvTime.setText(event.getStartTime() + " - " + event.getEndTime());
            tvVenue.setText(event.getVenueName());
            tvDescription.setText(event.getDescription());
            tvPrice.setText(String.format(Locale.getDefault(), "€%.2f", event.getPrice()));

            tvBadge.setVisibility(event.isRegistrationRequired() ? View.VISIBLE : View.GONE);

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