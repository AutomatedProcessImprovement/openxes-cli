package org.example;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XParserRegistry;
import org.deckfour.xes.model.XLog;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("f", "from", true, "Input file path");
        options.addOption("t", "to", true, "Output format extension");
        options.addOption("o", "output", true, "Output file path (optional)");

        CommandLineParser argsParser = new DefaultParser();
        CommandLine cmd = argsParser.parse(options, args);

        String inputPath = cmd.getOptionValue("f");
        if (inputPath.isEmpty()) {
            throw new Exception("Input path is empty");
        }

        File source = new File(inputPath);
        if (!source.exists()) {
            throw new Exception("Input file does not exist");
        }

        String outputFormat = cmd.getOptionValue("t");
        if (!outputFormat.equalsIgnoreCase("xes") && !outputFormat.equalsIgnoreCase("csv")) {
            throw new Exception("Output format is not supported");
        }

        String outputPath = cmd.getOptionValue("o");
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = inputPath.substring(0, inputPath.lastIndexOf('.')) + ".csv";
        }

        File destination = new File(outputPath);
        if (destination.exists()) {
            System.out.println("Output file already exists, it would be overwritten");
        }

        if (outputFormat.equalsIgnoreCase("csv")) {
            xesToCsv(source, destination);
        } else if (outputFormat.equalsIgnoreCase("xes")) {
            csvToXes(source, destination);
        } else {
            throw new Exception("Output format is not supported");
        }
    }

    private static void xesToCsv(File source, File destination) throws Exception {
        Collection<XLog> logs = null;
        for (XParser parser : XParserRegistry.instance().getAvailable()) {
            if (parser.canParse(source)) {
                System.out.println("Using input parser: " + parser.name());
                logs = parser.parse(source);
                break;
            }
        }
        assert logs != null;
        System.gc();

        CsvSerializer serializer = new CsvSerializer();
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        serializer.serialize(logs.iterator().next(), output);
        output.flush();
        output.close();
    }

    private static void csvToXes(File source, File destination) throws Exception {
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

        List<String> columns = records.iterator().next().stream().toList();
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