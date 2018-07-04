package coverage;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Very small test for coverage generation.
 */
public class CoverageGeneratorTest {
    private static final String BAM_PATH = "./src/test/resources/P2_TB_SRR833154_H37Rv_BR.bam";


    /**
     * Test running on threads vs without.
     * @throws IOException if something crashed.
     */
    @Test
    @Ignore
    public void testThreads() throws IOException {
        Coverage coverage = new CoverageGenerator().bamFileBasicCoverage(
                BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 10000);
        Coverage coverage1 = new CoverageGenerator().bamFileBasicCoverageThreads(
                8, BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 10000);
        assertThat(coverage.getTotalCovered()).isEqualTo(coverage1.getTotalCovered());
    }

    /**
     * Test with 1 thread.
     * @throws IOException if something crashed.
     */
    @Test
    @Ignore
    public void test1Threads() throws IOException {
        Coverage coverage = new CoverageGenerator().bamFileBasicCoverage(
                BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 10000);
        Coverage coverage1 = new CoverageGenerator().bamFileBasicCoverageThreads(
                1, BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 10000);
        assertThat(coverage.getTotalCovered()).isEqualTo(coverage1.getTotalCovered());
    }
}
