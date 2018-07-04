package examples;

import lombok.Getter;

import java.io.File;
import java.net.URL;

/**
 * Class to easy create example use cases.
 */
class ExampleTest {
    @Getter
    private String testName;
    private String explanation;
    @Getter
    private String commandInput;
    private String commandOutput;

    /**
     * Basic constructor initializing fields.
     *
     * @param name        the test / example name field.
     * @param explanation the example explanation.
     * @param input       the command line input.
     * @param output      the expected command output.
     */
    ExampleTest(String name, String explanation, String input, String output) {
        this.explanation = explanation;
        this.commandOutput = output;
        this.commandInput = input;
        this.testName = name;
    }

    /**
     * Run this 'example' and return the specified output to show the user.
     * The output will be nicely formatted with name, input, output and fail/pass.
     *
     * @return the output to show the user.
     */
    public String exampleOutput() {
        String output = getRunOutput();
        if (output == null) {
            return "Running example failed! UTF-8 encoding was not supported.";
        }
        boolean equalOutput = equalsTrueOut(output);
        String preOutString = "";
        if (explanation != null) {
            preOutString += explanation + "\n";
        }
        preOutString += "Executing: '" + commandInput + "'\n\n";
        return preOutString + output + "\n\nWhich is " + (equalOutput ? "" : "in")
                + "correct according to specifications.";
    }

    /**
     * Get the command output by temporary capturing System.out.
     * Return null if the UTF-8 encoding was not supported.
     *
     * @return the string which was the output of the command.
     */
    String getRunOutput() {
        String[] command = getCommand();
        String output = ExampleUtil.getRunOutput(command);
        //replace \r to make windows not **** up things.
        output = output.replaceAll("\r", "");
        //remove preceding new line characters.
        if (output.endsWith("\n")) {
            output = output.substring(0,
                    output.length() - 1);
        }
        return output;
    }

    /**
     * Get the command by splitting the string and replacing the variables.
     *
     * @return the command to execute.
     */
    protected String[] getCommand() {
        String[] result = this.commandInput.split("\\s");
        for (int i = 0; i < result.length; i++) {
            if (result[i].startsWith("$")) {
                result[i] = getVariableValue(result[i].substring(1));
            }
        }
        return result;
    }

    private String getVariableValue(String variable) {
        String lookedUp = Examples.VARIABLE_MAP.get(variable);
        if (lookedUp == null) {
            return variable;
        }
        if (lookedUp.startsWith("FILE//")) {
            URL path = getClass().getClassLoader().getResource(
                    lookedUp.substring(6));
            if (path == null) {
                return lookedUp.substring(6);
            }
            File file = new File(path.getFile());
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            return lookedUp.substring(6);
        }
        return lookedUp;
    }

    /**
     * Check if the output we got is correct.
     *
     * @param output the output we got.
     * @return true in case it is right.
     */
    private boolean equalsTrueOut(String output) {
        String expected = getExpectedOutput();
        if (expected.length() < 1) {
            return false;
        }
        return output.startsWith(expected);
    }

    /**
     * Get expected output.
     *
     * @return the command output.
     */
    protected String getExpectedOutput() {
        return this.commandOutput;
    }
}
