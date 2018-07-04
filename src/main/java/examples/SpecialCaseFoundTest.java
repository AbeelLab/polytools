package examples;

import java.io.*;

/**
 * Example test class for cases found by special case finder.
 */
public class SpecialCaseFoundTest extends ExampleTest {
    /**
     * Basic constructor initializing fields.
     *
     * @param name        the test / example name field.
     * @param explanation the example explanation.
     * @param input       the command line input.
     */
    SpecialCaseFoundTest(String name, String explanation, String input) {
        super(name, explanation, input, null);
    }

    /**
     * Get command.
     *
     * @return the command to execute.
     */
    @Override
    protected String[] getCommand() {
        return new String[]{"consensus", "-c", "\"" + getCommandInput() + "\""};
    }

    /**
     * Get the run output.
     *
     * @return the real output.
     */
    @Override
    String getRunOutput() {
        String out = super.getRunOutput();
        int lastNotDotIndex = out.length() - 1;
        while (out.charAt(lastNotDotIndex) == '.'
                || out.charAt(lastNotDotIndex) == '\n'
                || out.charAt(lastNotDotIndex) == '\r') {
            lastNotDotIndex--;
        }
        return out.substring(0, lastNotDotIndex + 1);
    }

    /**
     * Get expected output.
     *
     * @return the output from result field.
     */
    @Override
    protected String getExpectedOutput() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(
                        getCommandInput()), "UTF-8"))) {
            String nextLine = reader.readLine();
            while (nextLine != null) {
                nextLine = reader.readLine();
                if (nextLine == null) {
                    return "";
                }
                if (nextLine.startsWith("##RESULT=")) {
                    return nextLine.substring(9);
                }
                //ignored tests basically
                if (nextLine.startsWith("##/RESULT=")) {
                    return "!" + nextLine.substring(10);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
