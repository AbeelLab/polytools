package cli;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Some regression tests for the antisense thingy.
 */
public class ConsensusCommandReverseTest {

    private static final String VCF_FILE = "src/test/resources/reverse/sample.vcf";
    private static final String PYLON_FILE = "src/test/resources/reverse/pylon.vcf";
    private static final String FASTA_FILE = "src/test/resources/reverse/sample.fasta";
    private static final String GFF3_FILE = "src/test/resources/reverse/sample.gff3";
    private PrintStream stdout;
    private ByteArrayOutputStream stream;

    /**
     * Initialize the outputstreams.
     */
    @Before
    public void init() {
        stdout = System.out;
        stream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(stream, true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            fail("Unsupported encoding", e);
        }
    }

    /**
     * Clean up the outputstreams.
     */
    @After
    public void cleanUp() {
        System.setOut(stdout);
    }

    /**
     * Test normal.
     */
    @Test
    public void testNormal() {
        //TGACCGATGA -> TAGCCCCAGA
        Cli.launch("consensus", "-c", VCF_FILE, "-a", GFF3_FILE, "superOperon", "-f", FASTA_FILE, "-col", "0");
        try {
            assertThat(stream.toString("UTF-8")).contains("TAGCCCCAGA");
        } catch (UnsupportedEncodingException e) {
            fail("Unspported encoding", e);
        }
    }

    /**
     * Test an antisense with an standard vcf.
     */
    @Test
    public void testReverse() {
        // a <-> t g <-> c
        //TGACCGATGA -> TAGCCCCAGA
        Cli.launch("consensus", "-c", VCF_FILE, "-a", GFF3_FILE, "superOperonRev", "-f", FASTA_FILE, "-col", "0");
        try {
            assertThat(stream.toString("UTF-8")).contains("TCTGGGGCTA");
        } catch (UnsupportedEncodingException e) {
            fail("Unspported encoding", e);
        }
    }

    /**
     * Test antisense with an pylon vcf.
     */
    @Test
    public void testReversePylonWithFasta() {
        Cli.launch("consensus", "-c", PYLON_FILE, "-a", GFF3_FILE, "superOperonRev", "-f", FASTA_FILE, "-col", "0");
        try {
            assertThat(stream.toString("UTF-8")).contains("TCTGGGGCTA");
        } catch (UnsupportedEncodingException e) {
            fail("Unspported encoding", e);
        }
    }

    /**
     * Test an anitesense with an pylon vcf withouth the fasta.
     */
    @Test
    public void testReversePylonWithoutFasta() {
        Cli.launch("consensus", "-c", PYLON_FILE, "-a", GFF3_FILE, "superOperonRev", "-noc", "0");
        try {
            assertThat(stream.toString("UTF-8")).contains("TCTGGGGCTA");
        } catch (UnsupportedEncodingException e) {
            fail("Unspported encoding", e);
        }
    }
}
