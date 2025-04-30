package com.devappsys.logs_grpc.models.data;

import com.devappsys.log.Event;
import com.devappsys.log.Event.EventMessage;
import com.google.protobuf.Timestamp;
import java.util.Map;

public class EventModel {

    private String eventID;
    private String eventName;
    private String screenName;
    private Timestamp eventTime;
    private boolean appOpened;
    private boolean appBackgrounded;
    private boolean sessionStarted;
    private boolean sessionEnded;
    private double latitude;
    private double longitude;
    private String city;
    private String region;
    private String country;
    private String carrier;
    private boolean dynamicConfigChanged;
    private Map<String, Object> customAttributes;

    // Constructor
    public EventModel(String eventID, String eventName, String screenName, Timestamp eventTime,
                      boolean appOpened, boolean appBackgrounded, boolean sessionStarted,
                      boolean sessionEnded, double latitude, double longitude, String city,
                      String region, String country, String carrier, boolean dynamicConfigChanged,
                      Map<String, Object> customAttributes) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.screenName = screenName;
        this.eventTime = eventTime;
        this.appOpened = appOpened;
        this.appBackgrounded = appBackgrounded;
        this.sessionStarted = sessionStarted;
        this.sessionEnded = sessionEnded;
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.region = region;
        this.country = country;
        this.carrier = carrier;
        this.dynamicConfigChanged = dynamicConfigChanged;
        this.customAttributes = customAttributes;
    }

    // Method to convert EventModel to Protobuf EventMessage
    public EventMessage toProtobuf() {
        EventMessage.Builder eventMessageBuilder = EventMessage.newBuilder();

        eventMessageBuilder.setEventID(this.eventID)
                .setEventName(this.eventName)
                .setScreenName(this.screenName)
                .setEventTime(this.eventTime)
                .setAppOpened(this.appOpened)
                .setAppBackgrounded(this.appBackgrounded)
                .setSessionStarted(this.sessionStarted)
                .setSessionEnded(this.sessionEnded)
                .setLatitude(this.latitude)
                .setLongitude(this.longitude)
                .setCity(this.city)
                .setRegion(this.region)
                .setCountry(this.country)
                .setCarrier(this.carrier)
                .setDynamicConfigChanged(this.dynamicConfigChanged)
//                .putAllCustomAttributes(this.customAttributes)
        ;

        return eventMessageBuilder.build();
    }

    // Getters and Setters

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public boolean isDynamicConfigChanged() {
        return dynamicConfigChanged;
    }

    public void setDynamicConfigChanged(boolean dynamicConfigChanged) {
        this.dynamicConfigChanged = dynamicConfigChanged;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean isSessionEnded() {
        return sessionEnded;
    }

    public void setSessionEnded(boolean sessionEnded) {
        this.sessionEnded = sessionEnded;
    }

    public boolean isSessionStarted() {
        return sessionStarted;
    }

    public void setSessionStarted(boolean sessionStarted) {
        this.sessionStarted = sessionStarted;
    }

    public boolean isAppBackgrounded() {
        return appBackgrounded;
    }

    public void setAppBackgrounded(boolean appBackgrounded) {
        this.appBackgrounded = appBackgrounded;
    }

    public boolean isAppOpened() {
        return appOpened;
    }

    public void setAppOpened(boolean appOpened) {
        this.appOpened = appOpened;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}