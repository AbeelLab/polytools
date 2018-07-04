package cli;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.assertEquals;

/**
 * Tests usage of a config file.
 */
public class CommandConfigFileTest {

    private static final String VCF_FILE = "src/test/resources/ConsensusGenerator/sample.vcf";
    private static final String FASTA_FILE = "src/test/resources/ConsensusGenerator/sample.fasta";
    private static final String CONF_PATH = "src/test/resources/cli/";

    /**
     * Checks that arguments not provided on the command line are retrieved from the config file.
     * and that commented lines in the config file are ignored.
     */
    @Test
    public void successfulConfigOption() {
        Command consensus = new ConsensusCommand(
            new String[] {"-f", FASTA_FILE, "-c", VCF_FILE, "-conf", CONF_PATH + "values.config"});
        assertEquals(0, consensus.parseArgs());
        assertTrue(consensus.getOptionValues("region").length == 2);
    }

    /**
     * Checks that options in the config file are ignored if also present on the command line.
     */
    @Test
    public void commandLinePrecedence() {
        Command consensus = new ConsensusCommand(
            new String[] {"-f", FASTA_FILE, "-c", VCF_FILE, "-r", "5", "-conf", CONF_PATH + "values.config"});
        consensus.parseArgs();
        assertEquals(0, consensus.parseArgs());
        assertTrue(consensus.getOptionValues("region").length == 1);
        assertTrue(consensus.getOptionValue("region").equals("5"));
    }

    /**
     * Checks that program ends with an error if the config file contains an unsupported option.
     */
    @Test
    public void nonExistingOption() {
        Command consensus = new ConsensusCommand(
            new String[] {"-f", FASTA_FILE, "-c", VCF_FILE, "-conf", CONF_PATH + "values2.config"});
        assertEquals(1, consensus.parseArgs());
    }
}
