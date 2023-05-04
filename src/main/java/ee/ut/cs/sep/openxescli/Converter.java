package ee.ut.cs.sep.openxescli;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.model.XLog;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Converter {
    public static void xesToCsv(File source, File destination) throws Exception {
        Collection<XLog> logs = null;
        for (XParser parser : XParserRegistry.instance().getAvailable()) {
            if (parser.canParse(source)) {
                logs = parser.parse(source);
                break;
            }
        }
        if (logs == null) {
            throw new Exception("Could not find a parser for the given file");
        }
        System.gc();

        CsvSerializer serializer = new CsvSerializer();
        OutputStream output = new BufferedOutputStream(Files.newOutputStream(destination.toPath()));
        serializer.serialize(logs.iterator().next(), output);
        output.flush();
        output.close();
    }

    public static void csvToXes(File source, File destination) throws Exception {
        try {
            EventLog log = parseEventLogFromCsv(source);
            log.writeToFile(destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static EventLog parseEventLogFromCsv(File source) throws IOException {
        FileReader reader = new FileReader(source);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);

        // Getting columns

        List<String> columns = records.iterator().next().stream().collect(Collectors.toList());
        HashMap<String, Integer> columnsMap = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            columnsMap.put(columns.get(i), i);
        }

        // Parsing CSV file

        EventLog log = new EventLog();
        for (CSVRecord record : records) {
            String caseId = record.get(columnsMap.get("case:concept:name"));
            String activity = record.get(columnsMap.get("concept:name"));
            String resource = record.get(columnsMap.get("org:resource"));
            String startTimestamp = record.get(columnsMap.get("start_timestamp"));
            String endTimestamp = record.get(columnsMap.get("time:timestamp"));

            Event event = new Event(caseId, activity, resource, startTimestamp, endTimestamp);
            log.addEvent(caseId, event);
        }

        return log;
    }
}
