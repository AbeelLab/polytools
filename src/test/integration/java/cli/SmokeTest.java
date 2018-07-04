package cli;

import general.IntegrationTest;
import logger.LogLevel;
import logger.MultiLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the program as a user it would use.
 */
public class SmokeTest extends IntegrationTest {
    private static final String VCF_FIL3 = "src/test/resources/ConsensusGenerator/sample3.vcf";
    private static final String VCF_FILE4 = "src/test/resources/ConsensusGenerator/sample4.vcf";
    private static final String VCF_FILE5 = "src/test/resources/ConsensusGenerator/sample5.vcf";
    private static final String VCF_FILE7 = "src/test/resources/ConsensusGenerator/sample7.vcf";
    private static final String VCF_FILE8 = "src/test/resources/ConsensusGenerator/sample8.vcf";
    private static final String VCF_FILE9 = "src/test/resources/ConsensusGenerator/sample9.vcf";
    private static final String VCF_FILE10 = "src/test/resources/ConsensusGenerator/sample10.vcf";

    private static final String STRING_FORMAT = "UTF-8";

    private ByteArrayOutputStream outputStream;
    private PrintStream stdout;

    /**
     * Replace the System.out stream by a stream that we can easily read.
     */
    @Before
    public void init() {
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
     * @return The value computed by the program.
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

    /**
     * Smoketest for help function.
     */
    @Test
    public void smokeTestHelp() {
        MultiLogger.get().setStdLogLevel(LogLevel.NOTHING);
        Cli.launch("-h");
        String result = getResult();
        assertThat(result).startsWith(Cli.getHelp());
    }

    /**
     * Homozygous test
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestHomozygous() {
        Cli.launch("consensus", "-c", VCF_FIL3, "-se");
        String result = getResultWithoutHeader();
        String expected = "..AG..CCA........................................A..........";
        assertEquals(expected, result);
    }

    /**
     * Test color replacement 1.
     */
    @Test
    public void smokeTestColorInChanges() {
        Cli.launch("consensus", "-c", VCF_FIL3, "-col", "3", "-se");
        String result = getResultWithoutHeader();
        String expected = "..\u001B[35mA\u001B[0m\u001B[36mG\u001B[0m..\u001B[32mC\u001B[0m\u001B"
                + "[32mC\u001B[0m\u001B[35mA\u001B[0m........................................\u001B"
                + "[35mA\u001B[0m..........";
        assertEquals(expected, result);
    }

    /**
     * Heterozygous test
     * Tests on the length and the content of a heterozygous replace.
     */
    @Test
    public void smokeTestHeterozygous() {
        Cli.launch("consensus", "-c", VCF_FILE4, "-col", "0", "-se");
        String result = getResultWithoutHeader();

//        assertThat(result.length()).isEqualTo(60);

        String expected = "..RR..SMW........................................W.........W";

        assertEquals(expected, result);
    }

    /**
     * Homozygous test
     * Tests on the length and the content of a ref replace.
     */
    @Test
    public void smokeTestHomozygous2() {
        int err = Cli.launch("consensus", "-c", VCF_FILE5, "-col", "0", "-se");
        assertThat(err).isEqualTo(0);
        String result = getResultWithoutHeader();

        String expected = "..GA..GAT........................................T..........";

        assertEquals(expected, result);
    }

    /**
     * Deletion encoding test with overlapping ref.
     * Are overlapping references possible?
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestDeletionOverlap() {
        Cli.launch("consensus", "-c", VCF_FILE7, "-col", "0", "-se");

        String result = getResultWithoutHeader();

//        assertEquals(50 + 2, result.length());

        String expected = "..AR[G].C...........................................";

        assertEquals(expected, result);

    }

    /**
     * Deletion encoding test
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestDeletion() {
        Cli.launch("consensus", "-c", VCF_FILE7, "-col", "0", "-se");
        String result = getResultWithoutHeader();
        String expected = "..AR[G].C...........................................";
        assertEquals(expected, result);
    }

    /**
     * Deletion encoding test
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestDeletionBig() {
        Cli.launch("consensus", "-c", VCF_FILE8, "-col", "0", "-se");
        String result = getResultWithoutHeader();
        String expected = "..AG..STM[CT].......................................";
        assertEquals(expected, result);
    }

    /**
     * Insertion encoding test
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestInsertion() {
        Cli.launch("consensus", "-c", VCF_FILE9, "-col", "0", "-se");

        String result = getResultWithoutHeader();

        String expected = "..R(T)G..C...........................................";

        assertEquals(expected, result);
    }

    /**
     * Insertion encoding test
     * Tests on the length and the content of a replace.
     */
    @Test
    public void smokeTestInsertionBig() {
        Cli.launch("consensus", "-c", VCF_FILE10, "-col", "0", "-se");

        String result = getResultWithoutHeader();

        String expected = "..ARY(TA).C...........................................";

        assertEquals(expected, result);
    }

    /**
     * Test the keep only indels option.
     */
    @Test
    public void smokeTestKeepIndels() {
        Cli.launch("consensus", "-c", VCF_FILE10, "-col", "0", "--keep-only-indels", "-se");

        String result = getResultWithoutHeader();

        String expected = "..GRY(TA).G...........................................";

        assertThat(result).isEqualTo(expected);
    }

    /**
     * Test the remove indels option.
     */
    @Test
    public void smokeTestRemoveIndels() {
        Cli.launch("consensus", "-c", VCF_FILE10, "-col", "0", "--remove-indels", "-se");

        String result = getResultWithoutHeader();

        String expected = "..AAT.C...........................................";

        assertThat(result).isEqualTo(expected);
    }
}
