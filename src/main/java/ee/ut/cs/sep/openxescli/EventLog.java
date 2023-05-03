package ee.ut.cs.sep.openxescli;

import lombok.Data;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.*;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Data
public class EventLog {
    private HashMap<String, List<Event>> traces; // key: caseId, value: list of events


    public EventLog() {
        traces = new HashMap<>();
    }

    private static void updateTraceCaseIdIfNotPresent(String caseId, XAttributeMap traceAttributes) {
        if (traceAttributes.get("concept:name") == null) {
            XAttributeLiteralImpl caseIdAttribute = new XAttributeLiteralImpl("case:concept:name", caseId);
            traceAttributes.put("concept:name", caseIdAttribute);
        }
    }

    private static void addEndEventIfPresent(XTraceImpl trace, Event event) {
        Optional<XAttributeMap> endEventAttributes = event.toEndAttributes();
        if (endEventAttributes.isPresent()) {
            XEventImpl endEvent = new XEventImpl(endEventAttributes.get());
            trace.add(endEvent);
        }
    }

    private static void addStartEventIfPresent(XTraceImpl trace, Event event) {
        Optional<XAttributeMap> startEventAttributes = event.toStartAttributes();
        if (startEventAttributes.isPresent()) {
            XEventImpl startEvent = new XEventImpl(startEventAttributes.get());
            trace.add(startEvent);
        }
    }

    public void addEvent(String caseId, Event event) {
        List<Event> events = traces.getOrDefault(caseId, new ArrayList<>());
        events.add(event);
        traces.put(caseId, events);
    }

    public void writeToFile(File destination) throws Exception {
        XLog xlog = toXLog();
        XesXmlSerializer serializer = new XesXmlSerializer();
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        serializer.serialize(xlog, output);
        output.flush();
        output.close();
    }

    public XLog toXLog() {
        XAttributeMap logAttributes = new XAttributeMapImpl();
        XLog xlog = new XLogImpl(logAttributes);

        for (String caseId : getTraces().keySet()) {
            XTraceImpl trace = toXTrace(caseId);
            xlog.add(trace);
        }

        return xlog;
    }

    private XTraceImpl toXTrace(String caseId) {
        XAttributeMap traceAttributes = new XAttributeMapImpl();
        XTraceImpl trace = new XTraceImpl(traceAttributes);

        getTraces().get(caseId).forEach(event -> {
            updateTraceCaseIdIfNotPresent(caseId, traceAttributes);
            addStartEventIfPresent(trace, event);
            addEndEventIfPresent(trace, event);
        });

        return trace;
    }
}
