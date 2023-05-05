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

        try (CSVPrinter csvPrinter = new CSVPrinter(csvBuffer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("case:concept:name", "concept:name", "org:resource", "start_timestamp", "time:timestamp");

            xLog.forEach(xTrace -> {
                val caseId = xTrace.getAttributes().get("concept:name").toString();
                assert caseId != null && !caseId.isEmpty();

                // Group events by resource
                val resourceGroups = xTrace.stream().collect(Collectors.groupingBy(xEvent -> {
                    XAttributeMap attributes = xEvent.getAttributes();
                    return attributes.getOrDefault("org:resource", defaultResource).toString();
                }));

                // Group resource groups by activity
                resourceGroups.forEach((resource, events) -> {
                    val activityGroups = events.stream().collect(Collectors.groupingBy(xEvent -> {
                        XAttributeMap attributes = xEvent.getAttributes();
                        return attributes.getOrDefault("concept:name", defaultConceptName).toString();
                    }));

                    activityGroups.forEach((activity, events2) -> {
                        // Sort events by timestamp
                        events2.sort((xEvent1, xEvent2) -> ComparisonChain.start()
                                .compare(xEvent1.getAttributes().getOrDefault("time:timestamp", defaultTimestamp).toString(),
                                        xEvent2.getAttributes().getOrDefault("time:timestamp", defaultTimestamp).toString())
                                .result());

                        // Compose an event with start and end timestamps

                        Event csvEvent = new Event();

                        csvEvent.setCaseId(caseId);
                        csvEvent.setResource(resource);
                        csvEvent.setActivity(activity);

                        for (XEvent event : events2) {
                            XAttributeMap attributes = event.getAttributes();
                            String timestamp = attributes.getOrDefault("time:timestamp", defaultTimestamp).toString();
                            String transition = attributes.getOrDefault("lifecycle:transition", defaultTransition).toString().toLowerCase();

                            if (transition.equals("start")) {
                                csvEvent.setStartTimestamp(timestamp);
                            } else if (transition.equals("complete")) {
                                csvEvent.setEndTimestamp(timestamp);

                                // Append event to CSV
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
}
