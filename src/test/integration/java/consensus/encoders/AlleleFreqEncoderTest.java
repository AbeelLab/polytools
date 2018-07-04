package consensus.encoders;

import cli.Cli;
import general.IntegrationTest;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the program as a user it would use.
 */
@RunWith(JUnitParamsRunner.class)
public class AlleleFreqEncoderTest extends IntegrationTest {
    private static final String VCF_FILE1 = "src/test/resources/consensus/encoders/sample1.vcf";
    private static final String VCF_FILE2 = "src/test/resources/consensus/encoders/sample2.vcf";
    private static final String FASTA_FIL2 = "src/test/resources/consensus/encoders/sample2.fasta";

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     */
    @Test
    public void missingLowerUpper() {
        Cli.launch("consensus", "-c", VCF_FILE1, "-ea", "-se");
        String result = getResultLastLine();
        String expected = "Missing argument for option: ea";
        assertEquals(expected, result);
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     */
    @Test
    public void missingLowerUpperLongCommand() {
        Cli.launch("consensus", "-c", VCF_FILE1, "-af-encoder", "-se");
        String result = getResult().trim();
        result = result.substring(result.lastIndexOf("\n") + 1);
        String expected = "Missing argument for option: ea";
        assertEquals(expected, result);
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     */
    @Test
    public void comboCommandMissingLowerUpper() {
        Cli.launch("consensus", "-c", VCF_FILE1, "-ea", "-af-encoder", "-se");
        String result = getResultLastLine();
        String expected = "Missing argument for option: ea";
        assertEquals(expected, result);
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     */
    @Test
    public void comboCommand() {
        Cli.launch("consensus", "-c", VCF_FILE1, "-ea", "-af-encoder", "0", "-se");
        String result = getResultLastLine();
        String expected = "Missing argument for option: ea";
        assertEquals(expected, result);
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     *
     * @param bounds   the bounds that specify the Allele frequency.
     * @param expected the expected result of the command.
     */
    @Test
    @Parameters({"0-0.93, AGC", "0-0.94, RRS", "0-0.95, RRS", "0.93-1, RRS", "0.94-1, RRS",
            "0.95-1, GAG", "0.95-0.95, GAG", "0.93-0.93, AGC", "0.94-0.94, RRS"})
    public void singleRegion(String bounds, String expected) {
        Cli.launch("consensus", "-c", VCF_FILE1, "-ea", bounds, "-se");
        String result = getResultWithoutHeader();
        assertEquals(expected, result);
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     *
     * @param bounds   the bounds that specify the Allele frequency.
     * @param expected the expected result of the command.
     */
    @Test
    @Parameters({"0.94-0, Error", "0.01-0, Error"})
    public void lowerUpperWrong(String bounds, String expected) {
        Cli.launch("consensus", "-c", VCF_FILE1, "-ea", bounds, "-se");
        String result = getResultLastLine();
        assertTrue(result.startsWith(expected));
    }

    /**
     * Allele Frequency Encoder -ea –af-encoder <lowerbound> <upperbound>.
     *
     * @param bounds   the bounds that specify the Allele frequency.
     * @param expected the expected result of the command.
     */
    @Test
    @Parameters({"0-0.95, RRSW", "0-0, AGCA", "0-0.94, RRSW", "0-0.93, AGCA", "0.95-1, GAGT"})
    public void withFasta(String bounds, String expected) {
        Cli.launch("consensus", "-c", VCF_FILE2, "-f", FASTA_FIL2, "-ea", bounds, "-se");
        String result = getResultWithoutHeader();
        assertEquals(expected, result);
    }

}