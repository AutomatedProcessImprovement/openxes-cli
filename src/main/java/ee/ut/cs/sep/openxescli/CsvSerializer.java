package ee.ut.cs.sep.openxescli;

import com.google.common.collect.ComparisonChain;
import lombok.val;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.out.XSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class CsvSerializer implements XSerializer {
    public CsvSerializer() {
    }

    @Override
    public String getName() {
        return "CSV";
    }

    @Override
    public String getDescription() {
        return "CSV Serializer";
    }

    @Override
    public String getAuthor() {
        return "Ihar Suvorau";
    }

    @Override
    public String[] getSuffices() {
        return new String[]{"csv"};
    }

    @Override
    public void serialize(XLog xLog, OutputStream outputStream) throws IOException {
        val defaultConceptName = new XAttributeLiteralImpl("concept:name", "");
        val defaultResource = new XAttributeLiteralImpl("org:resource", "");
        val defaultTimestamp = new XAttributeLiteralImpl("time:timestamp", "");
        val defaultTransition = new XAttributeLiteralImpl("lifecycle:transition", "complete");

        // Sort traces by case id
        xLog.sort((xTrace1, xTrace2) -> ComparisonChain.start()
                .compare(xTrace1.getAttributes().getOrDefault("concept:name", defaultConceptName).toString(),
                        xTrace2.getAttributes().getOrDefault("concept:name", defaultConceptName).toString())
                .result());

        Appendable csvBuffer = new StringBuilder();

        // Compose CSV incrementally
        try (CSVPrinter csvPrinter = new CSVPrinter(csvBuffer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("case:concept:name", "concept:name", "org:resource", "start_timestamp", "time:timestamp");

            // Event log level
            xLog.forEach(xTrace -> {
                val caseId = xTrace.getAttributes().get("concept:name").toString();
                assert caseId != null && !caseId.isEmpty();

                val resourceGroups = xTrace.stream().collect(Collectors.groupingBy(xEvent -> {
                    XAttributeMap attributes = xEvent.getAttributes();
                    return attributes.getOrDefault("org:resource", defaultResource).toString();
                }));

                // Resource level
                resourceGroups.forEach((resource, events) -> {
                    val activityGroups = events.stream().collect(Collectors.groupingBy(xEvent -> {
                        XAttributeMap attributes = xEvent.getAttributes();
                        return attributes.getOrDefault("concept:name", defaultConceptName).toString();
                    }));

                    // Activity level
                    activityGroups.forEach((activity, events2) -> {
                        // Sort events by timestamp
                        events2.sort((xEvent1, xEvent2) -> ComparisonChain.start()
                                .compare(xEvent1.getAttributes().getOrDefault("time:timestamp", defaultTimestamp).toString(),
                                        xEvent2.getAttributes().getOrDefault("time:timestamp", defaultTimestamp).toString())
                                .result());

                        Event csvEvent = new Event();
                        csvEvent.setCaseId(caseId);
                        csvEvent.setResource(resource);
                        csvEvent.setActivity(activity);

                        XEvent[] startEvents = events2.stream().filter(xEvent -> {
                            XAttributeMap attributes = xEvent.getAttributes();
                            String transition = attributes.getOrDefault("lifecycle:transition", defaultTransition).toString().toLowerCase();
                            return transition.equals("start");
                        }).toArray(XEvent[]::new);

                        XEvent[] completeEvents = events2.stream().filter(xEvent -> {
                            XAttributeMap attributes = xEvent.getAttributes();
                            String transition = attributes.getOrDefault("lifecycle:transition", defaultTransition).toString().toLowerCase();
                            return transition.equals("complete");
                        }).toArray(XEvent[]::new);

                        if (startEvents.length == completeEvents.length) {
                            for (int i = 0; i < startEvents.length; i++) {
                                XEvent startEvent = startEvents[i];
                                XEvent completeEvent = completeEvents[i];

                                XAttributeMap startAttributes = startEvent.getAttributes();
                                XAttributeMap completeAttributes = completeEvent.getAttributes();

                                String startTimestamp = startAttributes.getOrDefault("time:timestamp", defaultTimestamp).toString();
                                String completeTimestamp = completeAttributes.getOrDefault("time:timestamp", defaultTimestamp).toString();

                                csvEvent.setStartTimestamp(startTimestamp);
                                csvEvent.setEndTimestamp(completeTimestamp);

                                try {
                                    csvPrinter.printRecord(csvEvent.getCaseId(), csvEvent.getActivity(), csvEvent.getResource(), csvEvent.getStartTimestamp(), csvEvent.getEndTimestamp());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else if (startEvents.length == 0) {
                            for (XEvent event : events2) {
                                XAttributeMap attributes = event.getAttributes();
                                String timestamp = attributes.getOrDefault("time:timestamp", defaultTimestamp).toString();

                                csvEvent.setEndTimestamp(timestamp);

                                try {
                                    csvPrinter.printRecord(csvEvent.getCaseId(), csvEvent.getActivity(), csvEvent.getResource(), csvEvent.getStartTimestamp(), csvEvent.getEndTimestamp());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    });
                });
            });
        }

        outputStream.write(csvBuffer.toString().getBytes());
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
