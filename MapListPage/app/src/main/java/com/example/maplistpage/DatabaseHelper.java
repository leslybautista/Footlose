package com.example.maplistpage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "senior_events.db";
    private static final int DATABASE_VERSION = 4;

    private final Context context;
    private final String databasePath;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.databasePath = context.getDatabasePath(DATABASE_NAME).getPath();
        copyDatabaseIfNeeded();
    }

    private void copyDatabaseIfNeeded() {
        File dbFile = new File(databasePath);
        if (!dbFile.exists()) {
            try {
                copyDatabase();
                Log.d("DatabaseHelper", "Database copied successfully");
            } catch (IOException e) {
                throw new RuntimeException("Error copying database", e);
            }
        }
    }

    private void copyDatabase() throws IOException {
        File dbFile = new File(databasePath);
        dbFile.getParentFile().mkdirs();

        try (InputStream input = context.getAssets().open(DATABASE_NAME);
             OutputStream output = new FileOutputStream(databasePath)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Prebuilt database, nothing to create
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        context.deleteDatabase(DATABASE_NAME);
        copyDatabaseIfNeeded();
    }

    /**
     * Base query to get all event info with venues and event types
     */
    private static final String BASE_EVENT_QUERY =
            "SELECT e.*, e.mobility_level," +
                    "v.name AS venue_name, v.address, v.latitude, v.longitude, v.wheelchair_accessible AS venue_accessible, " +
                    "et.name AS event_type, et.icon AS event_icon " +
                    "FROM events e " +
                    "JOIN venues v ON e.venue_id = v.id " +
                    "JOIN event_types et ON e.event_type_id = et.id ";

    private List<Event> queryEvents(String whereClause, String[] args) {
        String query = BASE_EVENT_QUERY;
        if (whereClause != null && !whereClause.isEmpty()) {
            query += " WHERE " + whereClause;
        }
        query += " ORDER BY e.event_date, e.start_time";

        List<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, args);

        if (cursor.moveToFirst()) {
            do {
                Event event = new Event();
                event.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow("event_type")));
                event.setEventTypeIcon(cursor.getString(cursor.getColumnIndexOrThrow("event_icon")));
                event.setVenueName(cursor.getString(cursor.getColumnIndexOrThrow("venue_name")));
                event.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                event.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
                event.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
                event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow("event_date")));
                event.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
                event.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("end_time")));
                event.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
                event.setFree(cursor.getInt(cursor.getColumnIndexOrThrow("is_free")) == 1);
                event.setRegistrationUrl(cursor.getString(cursor.getColumnIndexOrThrow("registration_url")));
                event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow("organizer")));
                event.setWheelchairAccessible(cursor.getInt(cursor.getColumnIndexOrThrow("wheelchair_accessible")) == 1);
                event.setVenueAccessible(cursor.getInt(cursor.getColumnIndexOrThrow("venue_accessible")) == 1);
                event.setMobilityRestrictions(cursor.getString(cursor.getColumnIndexOrThrow("mobility_restrictions")));
                event.setMobilityLevel(cursor.getString(cursor.getColumnIndexOrThrow("mobility_level")));
                event.setRegistrationRequired(cursor.getInt(cursor.getColumnIndexOrThrow("registration_required")) == 1);

                events.add(event);
            } while (cursor.moveToNext());
        }

        Log.d("DB_TEST", "queryEvents returned " + events.size() + " events. Query: " + query);

        cursor.close();
        return events;
    }

    // -------------------- Public methods --------------------

    /**
     * Get all events
     */
    public List<Event> getAllEvents() {
        return queryEvents(null, null);
    }

    /**
     * Get only free events
     */
    public List<Event> getFreeEvents() {
        return queryEvents("e.is_free = 1", null);
    }

    /**
     * Get events for this month
     */
    public List<Event> getThisMonthEvents() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // First day of current month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        String firstDay = sdf.format(cal.getTime());

        // Last day of current month
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        String lastDay = sdf.format(cal.getTime());

        Log.d("DatabaseHelper", "This month: " + firstDay + " to " + lastDay);

        return queryEvents("e.event_date >= ? AND e.event_date <= ?",
                new String[]{firstDay, lastDay});
    }

    /**
     * Get events by mobility level: "Standing", "Seating", or "Mixed"
     */
    public List<Event> getEventsByMobilityLevel(String mobilityLevel) {
        Log.d("DatabaseHelper", "Filtering by mobility level: " + mobilityLevel);
        return queryEvents("e.mobility_level = ?", new String[]{mobilityLevel});
    }

    /**
     * Get standing events (mobility_level = "Standing")
     */
    public List<Event> getStandingEvents() {
        return getEventsByMobilityLevel("Standing");
    }

    /**
     * Get seating events (mobility_level = "Seating")
     */
    public List<Event> getSeatingEvents() {
        return getEventsByMobilityLevel("Seated");
    }

    /**
     * Get mixed events (mobility_level = "Mixed")
     */
    public List<Event> getMixedEvents() {
        return getEventsByMobilityLevel("Mixed");
    }

    /**
     * Get events by type name
     */
    public List<Event> getEventsByType(String eventTypeName) {
        return queryEvents("et.name = ?", new String[]{eventTypeName});
    }


    /**
     * Get a single event by ID (for EventDetailActivity)
     */
    public Event getEventById(int eventId) {
        String query = BASE_EVENT_QUERY + " WHERE e.id = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(eventId)});

        Event event = null;
        if (cursor.moveToFirst()) {
            event = new Event();
            event.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
            event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
            event.setEventType(cursor.getString(cursor.getColumnIndexOrThrow("event_type")));
            event.setEventTypeIcon(cursor.getString(cursor.getColumnIndexOrThrow("event_icon")));
            event.setVenueName(cursor.getString(cursor.getColumnIndexOrThrow("venue_name")));
            event.setAddress(cursor.getString(cursor.getColumnIndexOrThrow("address")));
            event.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
            event.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
            event.setEventDate(cursor.getString(cursor.getColumnIndexOrThrow("event_date")));
            event.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow("start_time")));
            event.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow("end_time")));
            event.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow("price")));
            event.setFree(cursor.getInt(cursor.getColumnIndexOrThrow("is_free")) == 1);
            event.setRegistrationUrl(cursor.getString(cursor.getColumnIndexOrThrow("registration_url")));
            event.setOrganizer(cursor.getString(cursor.getColumnIndexOrThrow("organizer")));
            event.setWheelchairAccessible(cursor.getInt(cursor.getColumnIndexOrThrow("wheelchair_accessible")) == 1);
            event.setVenueAccessible(cursor.getInt(cursor.getColumnIndexOrThrow("venue_accessible")) == 1);
            event.setMobilityRestrictions(cursor.getString(cursor.getColumnIndexOrThrow("mobility_restrictions")));
            event.setMobilityLevel(cursor.getString(cursor.getColumnIndexOrThrow("mobility_level")));
            event.setRegistrationRequired(cursor.getInt(cursor.getColumnIndexOrThrow("registration_required")) == 1);

            Log.d("DatabaseHelper", "Event found: " + event.getTitle());
        } else {
            Log.w("DatabaseHelper", "No event found with ID: " + eventId);
        }

        cursor.close();
        return event;
    }
}
