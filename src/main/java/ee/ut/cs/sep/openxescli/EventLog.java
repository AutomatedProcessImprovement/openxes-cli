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

@Data
public class EventLog {
    private HashMap<String, List<Event>> traces; // key: caseId, value: list of events


    public EventLog() {
        traces = new HashMap<>();
    }

    public void addEvent(String caseId, Event event) {
        List<Event> events = traces.getOrDefault(caseId, new ArrayList<>());
        events.add(event);
        traces.put(caseId, events);
    }

    public XLog toXLog() {
        XAttributeMap logAttributes = new XAttributeMapImpl();
        XLog xlog = new XLogImpl(logAttributes);

        for (String caseId : getTraces().keySet()) {
            XAttributeMap traceAttributes = new XAttributeMapImpl();
            XTraceImpl trace = new XTraceImpl(traceAttributes);

            for (Event event : getTraces().get(caseId)) {
                XAttributeMap startEventAttributes = new XAttributeMapImpl();
                XAttributeMap endEventAttributes = new XAttributeMapImpl();

                if (traceAttributes.get("concept:name") == null) {
                    XAttributeLiteralImpl caseIdAttribute = new XAttributeLiteralImpl("case:concept:name", caseId);
                    traceAttributes.put("concept:name", caseIdAttribute);
                }

                XAttributeLiteralImpl activity = new XAttributeLiteralImpl("concept:name", event.getActivity());
                XAttributeLiteralImpl resource = new XAttributeLiteralImpl("org:resource", event.getResource());
                XAttributeLiteralImpl startTimestamp = new XAttributeLiteralImpl("time:timestamp", event.getStartTimestamp());
                XAttributeLiteralImpl startTransition = new XAttributeLiteralImpl("lifecycle:transition", "start");
                XAttributeLiteralImpl endTimestamp = new XAttributeLiteralImpl("time:timestamp", event.getEndTimestamp());
                XAttributeLiteralImpl completeTransition = new XAttributeLiteralImpl("lifecycle:transition", "complete");

                startEventAttributes.put("concept:name", activity);
                startEventAttributes.put("org:resource", resource);
                startEventAttributes.put("time:timestamp", startTimestamp);
                startEventAttributes.put("lifecycle:transition", startTransition);

                endEventAttributes.put("concept:name", activity);
                endEventAttributes.put("org:resource", resource);
                endEventAttributes.put("time:timestamp", endTimestamp);
                endEventAttributes.put("lifecycle:transition", completeTransition);

                XEventImpl startEvent = new XEventImpl(startEventAttributes);
                XEventImpl endEvent = new XEventImpl(endEventAttributes);

                trace.add(startEvent);
                trace.add(endEvent);
            }

            xlog.add(trace);
        }
        return xlog;
    }

    public void writeToFile(File destination) throws Exception {
        XLog xlog = toXLog();
        XesXmlSerializer serializer = new XesXmlSerializer();
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        serializer.serialize(xlog, output);
        output.flush();
        output.close();
    }
}
