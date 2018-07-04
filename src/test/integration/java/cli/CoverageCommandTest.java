package cli;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Test the coverage command.
 */
public class CoverageCommandTest {

    private static final String BAM_FILE = "src/test/resources/P2_TB_SRR833154_H37Rv_BR.bam";

    /**
     * Test a basic working example.
     */
    @Test
    public void testBasicWorking() {
        CoverageCommand command = new CoverageCommand(
                new String[]{"-b", BAM_FILE, "-c", "gi|561108321|ref|NC_018143.2|", "-r", "100-10000", "-fy", "-A"});
        assertThat(command.execute()).isEqualTo(0);
    }
}
