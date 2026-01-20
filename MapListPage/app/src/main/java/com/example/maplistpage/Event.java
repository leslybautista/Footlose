package com.example.maplistpage;
public class Event {

    private int id;
    private String title;
    private String description;

    // Event type
    private String eventType;
    private String eventTypeIcon;

    // Venue
    private String venueName;
    private String address;
    private double latitude;
    private double longitude;
    private boolean venueAccessible;

    // Time
    private String eventDate;
    private String startTime;
    private String endTime;

    // Price & registration
    private double price;
    private boolean isFree;
    private boolean registrationRequired;
    private String registrationUrl;

    // Organizer & accessibility
    private String organizer;
    private boolean wheelchairAccessible;
    private String mobilityRestrictions;

    // ✅ REQUIRED empty constructor
    public Event() {
    }

    // ---------------- GETTERS ----------------

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEventType() { return eventType; }
    public String getEventTypeIcon() { return eventTypeIcon; }

    public String getVenueName() { return venueName; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isVenueAccessible() { return venueAccessible; }

    public String getEventDate() { return eventDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public double getPrice() { return price; }
    public boolean isFree() { return isFree; }
    public boolean isRegistrationRequired() { return registrationRequired; }
    public String getRegistrationUrl() { return registrationUrl; }

    public String getOrganizer() { return organizer; }
    public boolean isWheelchairAccessible() { return wheelchairAccessible; }
    public String getMobilityRestrictions() { return mobilityRestrictions; }

    // ---------------- SETTERS ----------------

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }

    public void setEventType(String eventType) { this.eventType = eventType; }
    public void setEventTypeIcon(String eventTypeIcon) { this.eventTypeIcon = eventTypeIcon; }

    public void setVenueName(String venueName) { this.venueName = venueName; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setVenueAccessible(boolean venueAccessible) {
        this.venueAccessible = venueAccessible;
    }

    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public void setPrice(double price) { this.price = price; }
    public void setFree(boolean free) { isFree = free; }
    public void setRegistrationRequired(boolean registrationRequired) {
        this.registrationRequired = registrationRequired;
    }
    public void setRegistrationUrl(String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }

    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }
    public void setMobilityRestrictions(String mobilityRestrictions) {
        this.mobilityRestrictions = mobilityRestrictions;
    }
}
