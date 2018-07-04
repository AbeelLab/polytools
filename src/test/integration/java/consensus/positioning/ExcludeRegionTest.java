package consensus.positioning;

import cli.Cli;
import general.IntegrationTest;
import junitparams.Parameters;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the program as a user it would use.
 */

public class ExcludeRegionTest extends IntegrationTest {

    private static final String VCF_FIL1 = "src/test/resources/consensus/positioning/sample1.vcf";

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
