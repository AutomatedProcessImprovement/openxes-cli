package ee.ut.cs.sep.openxescli;

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
        // NOTE: This implementation assumes that start and complete events are immediately following each other.

        Appendable csvBuffer = new StringBuilder();

        try (CSVPrinter csvPrinter = new CSVPrinter(csvBuffer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord("case:concept:name", "concept:name", "org:resource", "start_timestamp", "time:timestamp");
            xLog.forEach(xTrace -> {
                Event csvEvent = new Event();

                for (int i = 0; i < xTrace.size(); i++) {
                    csvEvent.setCaseId(xTrace.getAttributes().get("concept:name").toString());

                    XEvent event = xTrace.get(i);
                    XAttributeMap attributes = event.getAttributes();
                    String timestamp = attributes.getOrDefault("time:timestamp", new XAttributeLiteralImpl("time:timestamp", "")).toString();
                    String activity = attributes.getOrDefault("concept:name", new XAttributeLiteralImpl("concept:name", "")).toString();
                    String resource = attributes.getOrDefault("org:resource", new XAttributeLiteralImpl("org:resource", "")).toString();
                    String transition = attributes.getOrDefault("lifecycle:transition", new XAttributeLiteralImpl("lifecycle:transition", "complete")).toString();  // start or complete, or only complete

                    if (transition.equals("start")) {
                        csvEvent.setStartTimestamp(timestamp);
                        csvEvent.setActivity(activity);
                        csvEvent.setResource(resource);
                    } else if (transition.equals("complete")) {
                        assert csvEvent.getActivity().equals(activity);
                        assert csvEvent.getResource().equals(resource);

                        csvEvent.setActivity(activity);
                        csvEvent.setResource(resource);
                        csvEvent.setEndTimestamp(timestamp);

                        try {
                            csvPrinter.printRecord(
                                    csvEvent.getCaseId(),
                                    csvEvent.getActivity(),
                                    csvEvent.getResource(),
                                    csvEvent.getStartTimestamp(),
                                    csvEvent.getEndTimestamp()
                            );
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
