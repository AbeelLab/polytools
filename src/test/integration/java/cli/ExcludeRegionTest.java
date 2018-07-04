package cli;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import logger.LogLevel;
import logger.MultiLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

/**
 * Tests the program as a user it would use.
 */
@RunWith(JUnitParamsRunner.class)
public class ExcludeRegionTest {
    private static final String VCF_FIL1 = "src/test/resources/cli/sample1.vcf";

    private static final String STRING_FORMAT = "UTF-8";

    private ByteArrayOutputStream outputStream;
    private PrintStream stdout;

    /**
     * Replace the System.out stream by a stream that we can easily read.
     */
    @Before
    public void init() {
        MultiLogger.init(false, false, null, null);
        MultiLogger.get().setStdLogLevel(LogLevel.NOTHING);

        outputStream = new ByteArrayOutputStream();
        stdout = System.out;
        try {
            PrintStream printStream = new PrintStream(outputStream, false, STRING_FORMAT);
            System.setOut(printStream);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            fail();
        }
    }

    /**
     * Closes the outputstream and resets the System.out.
     */
    @After
    public void cleanup() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setOut(stdout);
    }

    /**
     * @return a trim version of the value computed by the test.
     */
    private String getResultWithoutHeader() {
        String result = getResult();

        stdout.println(result);
        int firstNewLine = result.indexOf(System.lineSeparator());
        stdout.print(firstNewLine);

        result = result.substring(firstNewLine + 1);

        int secondNewline = result.indexOf(System.lineSeparator());

        result = result.substring(secondNewline);

        return result.replace(System.lineSeparator(), "");
    }

    /**
     * @return The value computed by the test.
     */
    private String getResult() {
        String result = "";

        try {
            result = outputStream.toString(STRING_FORMAT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result.trim();
    }

    /**
     * Exclude positions --exclude-positions <regions>.
     *
     * @param region   the region that needs to be excluded (the ref needs to be used).
     * @param expected the expected result of the command.
     */
    @Test
    @Parameters({"0, AGC", "0-1, GGC", "0-2, GAC", "0-3, GAG", "0-0, AGC", "1-1, GGC", "2-2, AAC", "3-3, AGG",
            "4-4, AGC", "1-2, GAC", "2-3, AAG", "3-4, AGG", "4-5, AGC"})
    public void regionVariantCall(String region, String expected) {
        Cli.launch("consensus", "-c", VCF_FIL1, "--exclude-positions", region, "-se");
        String result = getResultWithoutHeader().trim();
        assertEquals(expected, result);
    }

    /**
     * Exclude positions --exclude-positions <regions>.
     */
    @Test
    public void specifyRegionWrong() {
        Cli.launch("consensus", "-c", VCF_FIL1, "--exclude-positions", "1-0", "-se");
        String result = getResult();
        result = result.substring(result.indexOf("\n") + 1);
        String expected = "Error";
        assertTrue(result.startsWith(expected));
    }

    /**
     * Exclude positions --exclude-positions <regions>.
     */
    @Test
    public void wrongRegionFormat() {
        Cli.launch("consensus", "-c", VCF_FIL1, "--exclude-positions", "-1-0", "-se");
        String result = getResult();
        result = result.substring(result.indexOf("\n") + 1);
        String expected = "Error";
        assertTrue(result.startsWith(expected));
    }

    /**
     * Exclude positions --exclude-positions <regions>.
     *
     * @param region1  the region that needs to be excluded (the ref needs to be used).
     * @param region2  the region that needs to be excluded (the ref needs to be used).
     * @param expected the expected result of the command.
     */
    @Test
    @Parameters({"0-1, 1-2, GAC", "1-2, 3, GAG", "0-1, 3-4, GGG"})
    public void mulitpleRegions(String region1, String region2, String expected) {
        Cli.launch("consensus", "-c", VCF_FIL1, "--exclude-positions", region1, region2, "-se");
        String result = getResultWithoutHeader().trim();
        assertEquals(expected, result);
    }
}
