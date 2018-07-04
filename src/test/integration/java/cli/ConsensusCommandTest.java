package cli;

import consensus.ConsensusGenerator;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * The Consensus command test class.
 * Checks whether command line strings that do not comply
 * to the usage specification are noticed and handled.
 */
@RunWith(JUnitParamsRunner.class)
public class ConsensusCommandTest {

    private static final String VCF_FILE = "src/test/resources/ConsensusGenerator/sample.vcf";
    private static final String FASTA_FILE = "src/test/resources/ConsensusGenerator/sample.fasta";
    private static final String WRITE_FILE = "src/test/resources/ConsensusGenerator/write.fasta";
    private static final String GFF_FILE = "src/test/resources/annotations/sampleAnnotations.gff3";

    /**
     * Expect consensus to terminate unsuccessfully when no required options provided.
     */
    @Test
    public void emptyCommand() {
        ConsensusCommand cmd = new ConsensusCommand("");
        assertThat(cmd.execute()).isEqualTo(1);
    }

    /**
     * Sanity check.
     */
    @Test
    public void successfulCommand() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 1-10");
        assertThat(cmd.execute()).isEqualTo(0);
    }

    /**
     * Sanity check.
     */
    @Test
    public void successfulMultipleRegions() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 1-2 5-6");
        ConsensusGenerator generator = mock(ConsensusGenerator.class);
        ConsensusCommand spiedCMD = spy(cmd);
        try {
            doReturn(generator).when(spiedCMD).makeConsensusGenerator(any(), any(), any(),
                    any(), any());
            spiedCMD.execute();
            verify(generator, times(2)).write(isA(OutputStream.class),
                    isA(Region.class), any(), any());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Sanity check 2.
     */
    @Test
    public void successfulMultipleRegions2() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 1-2 5-7");
        assertThat(cmd.execute()).isEqualTo(0);
    }

    /**
     * Tests if overlapping regions give an error.
     */
    @Test
    public void overlappingRegionsTest() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 1-2 2-7");
        assertThat(cmd.execute()).isEqualTo(0);
    }

    /**
     * Tests if cli works if no region is given.
     */
    @Test
    public void nonexistentRegionTest() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE);

        ConsensusGenerator generator = mock(ConsensusGenerator.class);
        ConsensusCommand spiedCMD = spy(cmd);
        try {
            doReturn(generator).when(spiedCMD).makeConsensusGenerator(any(), any(), any(),
                    any(), any());
            spiedCMD.execute();
            verify(generator, atLeastOnce()).write(isA(OutputStream.class),
                    isA(Region.class), any(), any());
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Tests if overlapping regions give an error.
     */
    @Test
    public void invertedRegionTest() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 1-2 7-2");
        assertThat(cmd.execute()).isEqualTo(1);
    }

    /**
     * Specification of non-existent file should be handled by returning error status code.
     */
    @Test
    public void nonExistentFile() {
        ConsensusCommand cmd = new ConsensusCommand("-f " + FASTA_FILE + " -c " + "dummy");
        assertThat(cmd.execute()).isEqualTo(1);
    }

    /**
     * Using an option that has an argument twice should result in error.
     */
    @Test
    public void ambiguousOption() {
        ConsensusCommand cmd = new ConsensusCommand(
                new String[]{"-f", FASTA_FILE, "-f", FASTA_FILE, "-c", VCF_FILE});
        assertThat(cmd.execute()).isEqualTo(1);
    }

    /**
     * Wrong region syntax should result in error.
     */
    @Test
    public void singleLocation() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r 10");
        assertThat(cmd.execute()).isEqualTo(0);
    }

    /**
     * Sanity check for well defined region.
     */
    @Test
    public void regionSyntaxError() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -r a-b");
        assertThat(cmd.execute()).isEqualTo(1);
    }

    /**
     * Sanity check that when specifying a file to writeAlt to, a file is actually created.
     */
    @Test
    public void fileWriteTest() {
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + FASTA_FILE + " -c " + VCF_FILE + " -o " + WRITE_FILE + " -r 1-2");
        cmd.execute();
        assertThat(new File(WRITE_FILE).exists()).isTrue();
    }

    /**
     * Test some malformed fasta files.
     * Currently only tested if it crashes or not.
     * TODO test the results.
     *
     * @param file The fasta file name.
     */
    @Test
    @Parameters({"bad1.fasta", "bad2.fasta", "bad3.fasta", "bad4.fasta", "bad5.fasta", "bad6.fasta"})
    public void test(String file) {
        String folder = "src/test/resources/ConsensusGenerator/";
        String fastaFile = folder + file;
        ConsensusCommand cmd = new ConsensusCommand("-f "
                + fastaFile + " -c " + VCF_FILE);
        assertThat(cmd.execute()).isEqualTo(0);
    }

    /**
     * Make sure that the file is not created when there is in invalid file.
     */
    @Test
    public void testNonExistingFile() {
        final String vcfName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        final String outName = "./src/test/resources/ConsensusGenerator/out.fasta";
        File vcf = new File(vcfName);
        File out = new File(outName);
        vcf.deleteOnExit();
        out.deleteOnExit();
        ConsensusCommand cmd = new ConsensusCommand("-o " + outName + " -c " + vcfName);
        cmd.execute();
        assertThat(out.exists()).isFalse();
    }

    /**
     * Make sure that the file is not created when there is in invalid file.
     */
    @Test
    public void testNonExistingFileSameName() {
        final String vcfName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        final String outName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        File vcf = new File(vcfName);
        File out = new File(outName);
        vcf.deleteOnExit();
        out.deleteOnExit();
        ConsensusCommand cmd = new ConsensusCommand("-o " + outName + " -c " + vcfName);
        cmd.execute();
        assertThat(out.exists()).isFalse();
    }

    /**
     * Make sure that the file is not created when there is in invalid file.
     */
    @Test
    public void testNonExistingFileFasta() {
        final String vcfName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        final String fastaName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        final String outName = "./src/test/resources/ConsensusGenerator/not-exists-vcf.vcf";
        File vcf = new File(vcfName);
        File out = new File(outName);
        File fasta = new File(fastaName);
        vcf.deleteOnExit();
        out.deleteOnExit();
        fasta.deleteOnExit();
        ConsensusCommand cmd = new ConsensusCommand("-f " + fastaName + " -o " + outName + " -c " + vcfName);
        cmd.execute();
        assertThat(out.exists()).isFalse();
    }

    /**
     * Removes the files created by these tests.
     *
     * @throws IOException throws an IOException to alert the developer
     *                     that there might be a problem in the file system.
     */
    @After
    public void tearDown() throws IOException {
        File file = new File(WRITE_FILE);
        if (file.exists() && !file.delete()) {
            throw new IOException("Unable to remove dump file");
        }
    }
}
