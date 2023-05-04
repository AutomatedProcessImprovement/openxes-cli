package ee.ut.cs.sep.openxescli;

import org.apache.commons.cli.*;

import java.io.File;

import static ee.ut.cs.sep.openxescli.Converter.csvToXes;
import static ee.ut.cs.sep.openxescli.Converter.xesToCsv;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("f", "from", true, "Input file path");
        options.addOption("t", "to", true, "Output format extension");
        options.addOption("o", "output", true, "Output file path (optional)");
        options.addOption("h", "help", false, "Print help message");

        CommandLineParser argsParser = new DefaultParser();
        CommandLine cmd = argsParser.parse(options, args);

        if (cmd.hasOption("h")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("openxes-cli", options);
            return;
        }

        String inputPath = cmd.getOptionValue("f");
        if (inputPath == null || inputPath.isEmpty()) {
            System.out.println("Input path is empty");
            return;
        }

        File source = new File(inputPath);
        if (!source.exists()) {
            System.out.println("Input file does not exist");
            return;
        }

        String outputFormat = cmd.getOptionValue("t");
        if (outputFormat == null ||
                !outputFormat.equalsIgnoreCase("xes") &&
                        !outputFormat.equalsIgnoreCase("csv")) {
            System.out.println("Output format is not supported");
            return;
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
            try {
                xesToCsv(source, destination);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (outputFormat.equalsIgnoreCase("xes")) {
            try {
                csvToXes(source, destination);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Output format is not supported");
        }
    }


}