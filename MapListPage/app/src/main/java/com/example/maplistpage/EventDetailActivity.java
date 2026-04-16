package com.example.maplistpage;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.LinearLayout;


import com.google.android.material.button.MaterialButton;

import android.app.AlertDialog;
import android.widget.ImageButton;
import com.airbnb.lottie.LottieAnimationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.view.LayoutInflater;

/**
 * EventDetailActivity displays comprehensive information about a specific dance event,
 * including location, price, and accessibility/mobility details.
 */
public class EventDetailActivity extends AppCompatActivity {

    private static final String TAG = "EventDetailActivity";

    private TextView tvDetailType;
    private TextView tvDetailTitle;
    private TextView tvDetailDescription;
    private TextView tvDetailDatetime;
    private TextView tvDetailVenue;
    private TextView tvDetailPrice;

    // New mobility and accessibility views
    private LinearLayout layoutMobilityLevel;
    private TextView tvMobilityIcon;
    private TextView tvMobilityDescription;

    private LinearLayout layoutAccessibility;
    private TextView tvAccessibilityInfo;

    private TextView tvMobilityRestrictions;

    private MaterialButton btnBackToList;
    private LottieAnimationView btnAddDorito;

    private DatabaseHelper dbHelper;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        initViews();

        // 1. DATA RETRIEVAL: Get the ID passed from the previous list activity
        int eventId = getIntent().getIntExtra("event_id", -1);

        Log.d(TAG, "Received event_id: " + eventId);

        // 2. ERROR HANDLING: If no ID is found, close the activity to prevent crashes
        if (eventId == -1) {
            Toast.makeText(this, "Error: Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load event data
        loadEventDetails(eventId);

        setupButtons();
    }


    /**
     * Finds and links all XML views to Java variables.
     */
    private void initViews() {
        tvDetailType = findViewById(R.id.tv_detail_type);
        tvDetailTitle = findViewById(R.id.tv_detail_title);
        tvDetailDescription = findViewById(R.id.tv_detail_description);
        tvDetailDatetime = findViewById(R.id.tv_detail_datetime);
        tvDetailVenue = findViewById(R.id.tv_detail_venue);
        tvDetailPrice = findViewById(R.id.tv_detail_price);

        // New mobility and accessibility views
        layoutMobilityLevel = findViewById(R.id.layout_mobility_level);
        tvMobilityIcon = findViewById(R.id.tv_mobility_icon);
        tvMobilityDescription = findViewById(R.id.tv_mobility_description);

        layoutAccessibility = findViewById(R.id.layout_accessibility);
        tvAccessibilityInfo = findViewById(R.id.tv_accessibility_info);

        tvMobilityRestrictions = findViewById(R.id.tv_mobility_restrictions);

        btnBackToList = findViewById(R.id.btn_back_to_list);
        btnAddDorito = findViewById(R.id.dorito_button);

        dbHelper = new DatabaseHelper(this);
    }
    /**
     * Logic for showing the Accessibility Help Dialog.
     * Uses a custom layout (dialog_help) and HTML formatting for clarity.
     */
    private void showHelpText() {
        LayoutInflater inflater = LayoutInflater.from(this); // Activity context
        View dialogView = inflater.inflate(R.layout.dialog_help, null, false);

        TextView txtHelp = dialogView.findViewById(R.id.txt_help);
        ImageButton btnClose = dialogView.findViewById(R.id.btn_close);

        String helpText =
                "Explore dance events easily:<br><br>"
                        + "• In a <b>Standing</b> event a medium level of mobility is expected"
                        + "• In a <b>Seated</b> event, seating areas will be available, no restrictions"
                        + "• In a <b>Mixed</b> event some level of movility is expected but seating area for breaks";

        txtHelp.setText(android.text.Html.fromHtml(helpText));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }


    private void loadEventDetails(int eventId) {
        currentEvent = dbHelper.getEventById(eventId);

        if (currentEvent == null) {
            Log.e(TAG, "Event not found in database for ID: " + eventId);
            Toast.makeText(this, "Event not found in database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Event loaded: " + currentEvent.getTitle());
        displayEventInfo();
    }


    /**
     * Map Event object properties to the UI.
     * Handles conditional styling (like Green text for FREE events).
     */
    private void displayEventInfo() {
        // type of event
        tvDetailType.setText(currentEvent.getEventType());

        // Title with emoji
        String titleWithEmoji = currentEvent.getEventTypeIcon() + " " + currentEvent.getTitle();
        tvDetailTitle.setText(titleWithEmoji);

        tvDetailDescription.setText(currentEvent.getDescription());

        // Date and time
        String dateTimeText = formatEventDateTime(
                currentEvent.getEventDate(),
                currentEvent.getStartTime(),
                currentEvent.getEndTime()
        );
        tvDetailDatetime.setText(dateTimeText);

        // Place (name + address, no accessibility)
        StringBuilder venueText = new StringBuilder();
        venueText.append(currentEvent.getVenueName()).append("\n");
        venueText.append(currentEvent.getAddress());
        tvDetailVenue.setText(venueText.toString());

        // Cost
        if (currentEvent.isFree()) {
            tvDetailPrice.setText("FREE");
            tvDetailPrice.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvDetailPrice.setText(String.format(Locale.getDefault(), "€%.2f", currentEvent.getPrice()));
            try {
                tvDetailPrice.setTextColor(getResources().getColor(R.color.purple_500));
            } catch (Exception e) {
                tvDetailPrice.setTextColor(getResources().getColor(android.R.color.holo_purple));
            }
        }

        displayMobilityInfo();
    }

    private void displayMobilityInfo() {
        // Verify mobility level
        if (currentEvent.getMobilityLevel() != null && !currentEvent.getMobilityLevel().isEmpty()) {
            layoutMobilityLevel.setVisibility(View.VISIBLE);

            String mobilityLevel = currentEvent.getMobilityLevel(); // Seated, Standing, Mixed
            String emoji = getMobilityEmoji(mobilityLevel);

            tvMobilityIcon.setText(emoji);

            tvMobilityDescription.setText(mobilityLevel);

            // Show mobility restrictions
            if (currentEvent.getMobilityRestrictions() != null &&
                    !currentEvent.getMobilityRestrictions().isEmpty()) {
                tvMobilityRestrictions.setText(currentEvent.getMobilityRestrictions());
                tvMobilityRestrictions.setVisibility(View.VISIBLE);
            } else {
                tvMobilityRestrictions.setVisibility(View.GONE);
            }

        } else {
            layoutMobilityLevel.setVisibility(View.GONE);
        }

        // Accessibility
        if (currentEvent.isWheelchairAccessible() && currentEvent.isVenueAccessible()) {
            layoutAccessibility.setVisibility(View.VISIBLE);
            tvAccessibilityInfo.setText("Wheelchair accessible venue");
        } else {
            layoutAccessibility.setVisibility(View.GONE);
        }


    }

    /**
     * Helper to return appropriate emoji based on mobility string.
     */
    private String getMobilityEmoji(String mobilityLevel) {
        if (mobilityLevel == null) return "ℹ️";

        switch (mobilityLevel) {
            case "Standing":
                return "🚶";
            case "Seating":
                return "🪑";
            case "Mixed":
                return "🔀";
            default:
                return "ℹ️";
        }
    }


    /**
     * Formats raw SQL date/time (YYYY-MM-DD) into human-friendly (Friday, Jan 22).
     */
    private String formatEventDateTime(String date, String startTime, String endTime) {
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.ENGLISH);

            Date dateObj = inputDateFormat.parse(date);
            String formattedDate = outputDateFormat.format(dateObj);

            String formattedStartTime = formatTime(startTime);
            String formattedEndTime = formatTime(endTime);

            return formattedDate + " • " + formattedStartTime + " - " + formattedEndTime;

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time", e);
            return date + " • " + startTime + " - " + endTime;
        }
    }

    private String formatTime(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }

        if (time.length() > 5) {
            return time.substring(0, 5);
        }

        return time;
    }

    private void setupButtons() {

        //  "Back to List"
        btnBackToList.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        //  "Add Dorito"
        btnAddDorito.setOnClickListener(v -> showHelpText());

    }

/**private void openMapLocation() {
 if (currentEvent == null) {
 Toast.makeText(this, "Event location not available", Toast.LENGTH_SHORT).show();
 return;
 }

 Log.d(TAG, "Opening map for: " + currentEvent.getVenueName());

 String uri = String.format(Locale.ENGLISH,
 "geo:%f,%f?q=%f,%f(%s)",
 currentEvent.getLatitude(),
 currentEvent.getLongitude(),
 currentEvent.getLatitude(),
 currentEvent.getLongitude(),
 Uri.encode(currentEvent.getVenueName())
 );

 Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
 intent.setPackage("com.google.android.apps.maps");

 if (intent.resolveActivity(getPackageManager()) != null) {
 startActivity(intent);
 } else {
 String browserUri = String.format(Locale.ENGLISH,
 "<a href="https://www.google.com/maps/search/?api=1&query=%f,%f">...</a>",
 currentEvent.getLatitude(),
 currentEvent.getLongitude()
 );
 Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
 startActivity(browserIntent);
 }
 }
 */



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}