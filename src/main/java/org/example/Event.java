package org.example;

import lombok.Data;

@Data
public class Event {
    private String caseId;
    private String activity;
    private String resource;
    private String startTimestamp;
    private String endTimestamp;
    public Event() {
    }
    public Event(String caseId, String activity, String resource, String startTimestamp, String endTimestamp) {
        this.caseId = caseId;
        this.activity = activity;
        this.resource = resource;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
    }
}
