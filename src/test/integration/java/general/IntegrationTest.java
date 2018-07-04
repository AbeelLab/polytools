package general;

import junitparams.JUnitParamsRunner;
import logger.LogLevel;
import logger.MultiLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.fail;

/**
 * A setup class for integration tests.
 */
@RunWith(JUnitParamsRunner.class)
public class IntegrationTest {

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
    protected String getResultWithoutHeader() {
        String result = getResult().trim();

        int headerEnd = result.lastIndexOf("|");


        return result.substring(headerEnd + 1).replace(System.lineSeparator(), "");
    }

    /**
     * @return only the last line of the result.
     */
    protected String getResultLastLine() {
        String result = getResult().trim();
        return result.substring(result.lastIndexOf("\n") + 1);
    }

    /**
     * @return The value computed by the test.
     */
    protected String getResult() {
        String result = "";

        try {
            result = outputStream.toString(STRING_FORMAT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return result;
    }

}
