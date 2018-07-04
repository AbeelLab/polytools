package coverage;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class for reading and testing a CoverageArray from bam file.
 */
public class CoverageArrayTest {
    private CoverageArray coverageArray;

    /**
     * Make the coverage array to test on.
     * @throws IOException if reader fails.
     */
    @Before
    public void makeCoverageArray() throws IOException {
        CoverageGenerator gen = new CoverageGenerator();
        coverageArray = gen.bamFileArrayCoverage(
                "./src/test/resources/P2_TB_SRR833154_H37Rv_BR.bam",
                "gi|561108321|ref|NC_018143.2|", 1000, 100000);
    }

    /**
     * Test getting the overview, even though this is mostly tested by hand.
     */
    @Test
    public void testOverview() {
        assertThat(coverageArray.coverageStatisticsOverview(4)
                .length()).isGreaterThan(40);
    }

    /**
     * Test calculating lowest coverage.
     */
    @Test
    public void testLowestCoverage() {
        assertThat(coverageArray.lowestCoverage()).isEqualTo(0);
    }

    /**
     * Test calculating highest coverage.
     */
    @Test
    public void testHighestCoverage() {
        assertThat(coverageArray.highestCoverage()).isEqualTo(46);
    }

    /**
     * Test if all bases have minimal 0 coverage by the minimal coverage method.
     */
    @Test
    public void testMinimal0() {
        assertThat(coverageArray.totalBasesWithMinimalCoverage(0))
                .isEqualTo(coverageArray.getTotalBases());
    }

    /**
     * Test is not all bases have at least 10 coverage.
     */
    @Test
    public void testMinimal10() {
        assertThat(coverageArray.totalBasesWithMinimalCoverage(10))
                .isLessThan(coverageArray.getTotalBases());
    }

    /**
     * Test if all bases have minimal 0 coverage by the minimal coverage method.
     */
    @Test
    public void testCoverageAtBase() {
        assertThat(coverageArray.coverageAtBase(1))
                .isEqualTo(22);
    }
}
