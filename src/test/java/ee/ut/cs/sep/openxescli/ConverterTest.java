package ee.ut.cs.sep.openxescli;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class ConverterTest {

    @Test
    void xesToCsvOne() throws Exception {
        File inputOne = new File("src/test/resources/BPIC15_5.xes.gz");
        File outputOne = new File("src/test/resources/output.csv");
        File outputTwo = new File("src/test/resources/output.xes");

        Converter.xesToCsv(inputOne, outputOne);
        assertTrue(outputOne.exists());

        Converter.csvToXes(outputOne, outputTwo);
        assertTrue(outputTwo.exists());

        outputOne.delete();
        outputTwo.delete();
    }

    @Test
    void xesToCsvTwo() throws Exception {
        File inputOne = new File("src/test/resources/PurchasingExample.xes.gz");
        File outputOne = new File("src/test/resources/pe_output.csv");
        File outputTwo = new File("src/test/resources/pe_output.xes");

        Converter.xesToCsv(inputOne, outputOne);
        assertTrue(outputOne.exists());

        Converter.csvToXes(outputOne, outputTwo);
        assertTrue(outputTwo.exists());

        outputTwo.delete();
        outputOne.delete();
    }

    @Test
    void csvToXes() {
    }
}