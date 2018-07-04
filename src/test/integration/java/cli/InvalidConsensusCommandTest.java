package cli;

import general.IntegrationTest;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test class, tests whether all command-line options are handled properly.
 */
@RunWith(JUnitParamsRunner.class)
public class InvalidConsensusCommandTest extends IntegrationTest {
    private static final String VCF = "src/test/resources/ConsensusGenerator/sample.vcf";

    /**
     * Test the execution of the consesus command.
     *
     * @param command  The command to execute.
     * @param exitCode The expected exitcode.
     * @param message  The expected error message.
     */
    @Test
    @Parameters(method = "parameters")
    public void execCommand(String command, int exitCode, String message) {
        ConsensusCommand consensusCommand = new ConsensusCommand(command.replaceAll("@VCF", VCF));
        int exec = consensusCommand.execute();
        assertThat(getResult()).contains(message);
        assertThat(exec).isEqualTo(exitCode);
    }

    /**
     * Gets the parameters for the parameterzied test.
     *
     * @return The parameters for the paremeterized tests.
     */
    public Object[] parameters() {
        return new Object[][]{
                {"-f sample.fasta", 1, "Missing required option: c"},
                {"-c A.vcf", 1, "Error interpreting command: Could not open file A.vcf"},
                {"-c @VCF -f sample.fasta", 1, "Error interpreting command: FASTA reference file does not exist:"},
                {"-c @VCF -ea", 1, "Missing argument for option: ea"},
                {"-c @VCF -ea a-b", 1, "AE encoder expects lower & upperbound to be numbers"},
                {"-c @VCF -ea no", 1, "AE encoder expects two numbers as values, usage: -ea <lower>-<upper>"},
                {"-c @VCF -ea 0", 1, "AE encoder expects two numbers as values, usage: -ea <lower>-<upper>"},
                {"-c @VCF -ea a", 1, "AE encoder expects two numbers as values, usage: -ea <lower>-<upper>"},
                {"-c @VCF -r a-b", 1, "Error interpreting command: Specified region is not valid"},
                {"-c @VCF -r no", 1, "Error interpreting command: Specified region is not valid"},
                {"-c @VCF --keep-filtered nonExists", 0, ">sampleGen|1-210|"},
                //Since you can have user defined filters there is npo real way to prevent this.
                {"-c @VCF --max-af x", 1, "Error interpreting command: --max-af needs an number as argument."},
                {"-c @VCF --min-af x", 1, "Error interpreting command: --min-af needs an number as argument."},
                {"-c @VCF --max-ac x", 1, "Error interpreting command: --max-ac needs an integer as argument."},
                {"-c @VCF --min-ac x", 1, "Error interpreting command: --min-ac needs an integer as argument."},
                {"-c @VCF --max-dp x", 1, "Error interpreting command: --max-dp needs an integer as argument."},
                {"-c @VCF --min-q x", 1, "Error interpreting command: --min-q needs a number as argument."},

        };
    }
}
