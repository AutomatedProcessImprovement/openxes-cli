package ee.ut.cs.sep.openxescli;

import lombok.Data;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

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

    public Optional<XAttributeMap> toStartAttributes() {
        if (startTimestamp == null || startTimestamp.isEmpty()) {
            return Optional.empty();
        }

        XAttributeMap attributes = new XAttributeMapImpl();

        XAttributeLiteralImpl activity = new XAttributeLiteralImpl("concept:name", getActivity());
        XAttributeLiteralImpl resource = new XAttributeLiteralImpl("org:resource", getResource());
        XAttributeLiteralImpl startTransition = new XAttributeLiteralImpl("lifecycle:transition", "start");

        attributes.put("concept:name", activity);
        attributes.put("org:resource", resource);
        attributes.put("lifecycle:transition", startTransition);

        Optional<Date> startTime = getStartTimestampAsDate();
        if (startTime.isPresent()) {
            XAttributeTimestampImpl startTimestamp = new XAttributeTimestampImpl("time:timestamp", startTime.get());
            attributes.put("time:timestamp", startTimestamp);
        } else {
            XAttributeLiteralImpl startTimestamp = new XAttributeLiteralImpl("time:timestamp", getStartTimestamp());
            attributes.put("time:timestamp", startTimestamp);
        }

        return Optional.of(attributes);
    }

    public Optional<XAttributeMap> toEndAttributes() {
        if (endTimestamp == null || endTimestamp.isEmpty()) {
            return Optional.empty();
        }

        XAttributeMap attributes = new XAttributeMapImpl();

        XAttributeLiteralImpl activity = new XAttributeLiteralImpl("concept:name", getActivity());
        XAttributeLiteralImpl resource = new XAttributeLiteralImpl("org:resource", getResource());
        XAttributeLiteralImpl completeTransition = new XAttributeLiteralImpl("lifecycle:transition", "complete");

        attributes.put("concept:name", activity);
        attributes.put("org:resource", resource);
        attributes.put("lifecycle:transition", completeTransition);

        Optional<Date> endTime = getEndTimestampAsDate();
        if (endTime.isPresent()) {
            XAttributeTimestampImpl endTimestamp = new XAttributeTimestampImpl("time:timestamp", endTime.get());
            attributes.put("time:timestamp", endTimestamp);
        } else {
            XAttributeLiteralImpl endTimestamp = new XAttributeLiteralImpl("time:timestamp", getEndTimestamp());
            attributes.put("time:timestamp", endTimestamp);
        }

        return Optional.of(attributes);
    }

    public Optional<Date> getStartTimestampAsDate() {
        return Optional.ofNullable(startTimestamp).flatMap(this::parseDate);
    }

    public Optional<Date> getEndTimestampAsDate() {
        return Optional.ofNullable(endTimestamp).flatMap(this::parseDate);
    }

    private Optional<Date> parseDate(String date) {
        return Optional.ofNullable(date).map(s -> {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                return dateFormat.parse(s);
            } catch (Exception e) {
                return null;
            }
        });
    }
}
