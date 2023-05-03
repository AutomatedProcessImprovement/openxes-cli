package org.example;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Trace {
    private String caseId;
    private List<Event> events;

    public Trace() {
        events = new ArrayList<>();
    }
}
