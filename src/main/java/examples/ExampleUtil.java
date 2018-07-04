package examples;

import cli.Cli;
import logger.LogLevel;
import logger.MultiLogger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Util class for examples.
 */
public class ExampleUtil {
    private ExampleUtil() {

    }

    /**
     * Get the command output by temporary capturing System.out.
     * Return null if the UTF-8 encoding was not supported.
     *
     * @param command the command to run.
     * @return the string which was the output of the command.
     */
    public static String getRunOutput(String[] command) {
        PrintStream oldSysOut = System.out;
        try {
            //setup redirecting output
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(byteOut, true, "UTF-8");
            System.setOut(stream);

            //run command and catch output, then place back std out
            MultiLogger.get().setStdLogLevel(LogLevel.NOTHING);
            Cli.launch(command);

            return byteOut.toString("UTF-8");
        } catch (UnsupportedEncodingException use) {
            return null;
        } finally {
            System.setOut(oldSysOut);
        }
    }
}
