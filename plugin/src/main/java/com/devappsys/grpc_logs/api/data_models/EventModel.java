package com.devappsys.grpc_logs.api.data_models;

import com.devappsys.log.Event.EventMessage;
import com.google.protobuf.Timestamp;

import java.util.Map;
import java.util.UUID;

public class EventModel {

    private String eventID;
    private String contextID;
    private String eventType;
    private Timestamp eventTime;
    private String displayName;
    private String message;
    private Map<String, Object> customAttributes;

    private String platformId;

    // Constructor

    public EventModel( String contextID, String eventType, Timestamp eventTime, String displayName, String message, String platformId, Map<String, Object> customAttributes) {
        this.eventID = UUID.randomUUID().toString();
        this.contextID = contextID;
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.displayName = displayName;
        this.message = message;
        this.platformId = platformId;
        this.customAttributes = customAttributes;
    }

    // Method to convert EventModel to Protobuf EventMessage
    public EventMessage toProtobuf() {
        EventMessage.Builder eventMessageBuilder = EventMessage.newBuilder();

        eventMessageBuilder.setUuid(eventID)
                .setContextId(contextID)
                .setEventType(eventType)
                .setDisplayName(displayName)
                .setMessage(message)
                .setEventTime(eventTime)
                .setPlatformId(platformId)
                .putAllEventProperties(getCustomAttributesToMap());

        return eventMessageBuilder.build();
    }

    // helpers
    public Map<String, String> getCustomAttributesToMap() {
        if (customAttributes == null || customAttributes.isEmpty()) {
            return new java.util.HashMap<>();
        }
        Map<String, String> customAttributesMap = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : customAttributes.entrySet()) {
            customAttributesMap.put(entry.getKey(), entry.getValue().toString());
        }
        return customAttributesMap;
    }

    // Getters and Setters

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Timestamp getEventTime() {
        return eventTime;
    }

    public void setEventTime(Timestamp eventTime) {
        this.eventTime = eventTime;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(Map<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }
}