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
                // Sort trace events by resource and activity
                xTrace.sort((xEvent1, xEvent2) -> ComparisonChain.start()
                        .compare(xEvent1.getAttributes().getOrDefault("org:resource", defaultResource).toString(),
                                xEvent2.getAttributes().getOrDefault("org:resource", defaultResource).toString())
                        .compare(xEvent1.getAttributes().getOrDefault("concept:name", defaultConceptName).toString(),
                                xEvent2.getAttributes().getOrDefault("concept:name", defaultConceptName).toString())
                        .result());

                // Process events

                Event csvEvent = new Event();

                for (int i = 0; i < xTrace.size(); i++) {
                    csvEvent.setCaseId(xTrace.getAttributes().get("concept:name").toString());

                    XEvent event = xTrace.get(i);
                    XAttributeMap attributes = event.getAttributes();
                    String timestamp = attributes.getOrDefault("time:timestamp", defaultTimestamp).toString();
                    String activity = attributes.getOrDefault("concept:name", defaultConceptName).toString();
                    String resource = attributes.getOrDefault("org:resource", defaultResource).toString();
                    String transition = attributes.getOrDefault("lifecycle:transition", defaultTransition).toString().toLowerCase();  // start or complete, or only complete

                    if (transition.equals("start")) {
                        csvEvent.setStartTimestamp(timestamp);
                        csvEvent.setActivity(activity);
                        csvEvent.setResource(resource);
                    } else if (transition.equals("complete")) {
                        assert csvEvent.getActivity() == null || csvEvent.getActivity().equals(activity);
                        assert csvEvent.getActivity() == null || csvEvent.getResource().equals(resource);

                        csvEvent.setActivity(activity);
                        csvEvent.setResource(resource);
                        csvEvent.setEndTimestamp(timestamp);

                        try {
                            csvPrinter.printRecord(csvEvent.getCaseId(), csvEvent.getActivity(), csvEvent.getResource(), csvEvent.getStartTimestamp(), csvEvent.getEndTimestamp());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        csvEvent = new Event();
                    }
                }
            });
        }

        outputStream.write(csvBuffer.toString().getBytes());
    }
}
