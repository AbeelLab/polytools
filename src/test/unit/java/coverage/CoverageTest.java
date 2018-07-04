package coverage;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class for testing Coverage, and reading a BAM file to a coverage object.
 */
public class CoverageTest {
    private static final String BAM_PATH = "./src/test/resources/P2_TB_SRR833154_H37Rv_BR.bam";

    private Coverage coverage;

    /**
     * Init basic coverage.
     * @throws IOException if reader fails.
     */
    @Before
    public void initCoverage() throws IOException {
        coverage = new CoverageGenerator().bamFileBasicCoverage(
                BAM_PATH,
                "gi|561108321|ref|NC_018143.2|", 1000, 10000);
    }

    /**
     * Test getting chromosome.
     */
    @Test
    public void testGetChromosome() {
        assertThat(coverage.getChromosome()).isEqualTo("gi|561108321|ref|NC_018143.2|");
    }

    /**
     * Test getting start pos.
     */
    @Test
    public void testGetStartPos() {
        assertThat(coverage.getStartPos()).isEqualTo(1000);
    }

    /**
     * Test getting end pos.
     */
    @Test
    public void testGetEndPos() {
        assertThat(coverage.getEndPos()).isEqualTo(10000);
    }

    /**
     * Test getting total depth.
     */
    @Test
    public void testGetTotalDepth() {
        assertThat(coverage.getTotalDepth()).isGreaterThan(10000);
    }

    /**
     * Test getting minimal coverage.
     */
    @Test
    public void testGetMinimalCoverage() {
        assertThat(coverage.getMinimalCoverage()).isEqualTo(0);
    }

    /**
     * Test getting total coverage.
     */
    @Test
    public void testGetTotalCoverage() {
        assertThat(coverage.getTotalCovered()).isEqualTo(9001);
    }

    /**
     * Test getting amount of bases.
     */
    @Test
    public void testGetTotalBases() {
        assertThat(coverage.getTotalBases()).isEqualTo(9001);
    }

    /**
     * Test getting average depth.
     */
    @Test
    public void testGetAverageDepth() {
        assertThat(coverage.getAverageDepth()).isGreaterThan(20);
    }

    /**
     * Test getting covered width.
     */
    @Test
    public void testGetCoveredWidth() {
        assertThat(coverage.getCoveredWidth()).isEqualTo(1D);
    }

    /**
     * Test to string method.
     */
    @Test
    public void testGetToString() {
        assertThat(coverage.toString()).startsWith("Coverage CHR");
    }

    /**
     * Test adding depth.
     * @throws IOException if reader fails.
     */
    @Test
    public void testAddDepth() throws IOException {
        coverage = new CoverageGenerator().bamFileBasicCoverage(
                BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 100000);

        long first = coverage.getTotalDepth();
        coverage.addToTotalDepth(1000);
        assertThat(first).isLessThan(coverage.getTotalDepth());
    }

    /**
     * Test adding covered.
     * @throws IOException if reader fails.
     */
    @Test
    public void testAddCovered() throws IOException {
        coverage = new CoverageGenerator().bamFileBasicCoverage(
                BAM_PATH, "gi|561108321|ref|NC_018143.2|", 1000, 100000);

        double first = coverage.getCoveredWidth();
        coverage.addToTotalCovered(100);
        assertThat(first).isLessThan(coverage.getCoveredWidth());
    }
}
