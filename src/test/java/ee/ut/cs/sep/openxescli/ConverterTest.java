package ee.ut.cs.sep.openxescli;

import tech.tablesaw.api.Table;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void xesToCsv3() throws Exception {
        File inputOne = new File("src/test/resources/Production.csv");
        File outputOne = new File("src/test/resources/Production.generated.xes");
        File outputTwo = new File("src/test/resources/Production.generated.csv");

        Converter.csvToXes(inputOne, outputOne);
        Converter.xesToCsv(outputOne, outputTwo);
        assertTrue(outputOne.exists());
        assertTrue(outputTwo.exists());

        Table tableOne = Table.read().csv(inputOne);
        Table tableTwo = Table.read().csv(outputTwo);
        assertEquals(tableOne.shape().split(": ")[1], tableTwo.shape().split(": ")[1]);
        assertEquals(tableOne.rowCount(), tableTwo.rowCount());

        outputOne.delete();
        outputTwo.delete();
    }

    @Test
    void csvToXes() {
    }
}