package examples;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test the Examples class examples.
 */
@RunWith(JUnitParamsRunner.class)
public class AcceptanceExampleTests {


    /**
     * Runs all the example test to see if they succeed or not.
     * @param test The example Tests.
     */
    @Test
    @Parameters(method = "getAllTests")
    public void runExampleTest(ExampleTest test) {
        System.out.println(test.getCommandInput());

        String testOutput = test.getRunOutput();
        testOutput = testOutput.replace("\r", "");
        testOutput = testOutput.replace("\n", "");
        
        System.out.println(testOutput);
        String expect = test.getExpectedOutput();
        expect = expect.replace("\r", "");
        expect = expect.replace("\n", "");
        assertThat(testOutput).endsWith(expect);
    }

    /**
     * Gets the example tests.
     * @return The array with the example tests.
     */
    public Object[] getAllTests() {
        LinkedList<ExampleTest> list = SpecialCaseFinder.getCases();
        list.addAll(Examples.createTests());
        return list.toArray();
    }
}
