package fasta;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract class for testing different fasta implementations.
 */
public abstract class FastaTest {
    /**
     * The fasta object to test, specific to implementation.
     */
    protected Fasta fasta;

    /**
     * Make the fasta object.
     */
    @Before
    public abstract void makeFasta();


    /**
     * Test reading a block.
     * @throws IOException when reading fails.
     */
    @Test
    public void testFastaGetBlock1() throws IOException {
        assertThat(new String(fasta.readBlockId(3), "UTF-8")).isEqualTo("GATCCTCCCGCCGGTGA"
                + "CCGATGACCGATCGGCTCCGCACGCGGACTCCATCGAGGCGGTCAAGGCCGCGCTCGACG"
                + "GCGCGCCGCCGATGCCCCCGCCGCGCGACCCGCTCGAGGAGGTCACGGCCGCGTTGGCCGCCC");
    }

    /**
     * Test reading a block.
     * @throws UnsupportedEncodingException of utf-8 suddenly disappears.
     */
    @Test
    public void testFastaGetBlock2() throws UnsupportedEncodingException {
        assertThat(new String(fasta.readBlockId(6), "UTF-8")).startsWith("TTTACGTCAGGCCGCTGCCC"
                + "ATCACCAGCAGACCGCCGACTACCTGCGGACCGTGCCGTCGTCGCACGAC"
                + "GCGATCCGCGAAAGTCTGGACTCGCTGGGGCCTATTTTCAGTG");
    }

    /**
     * Test reading a block.
     */
    @Test
    public void testFastaGetBlock3() {
        assertThat(fasta.readBlockId(6).length).isEqualTo(303_940);
    }

    /**
     * Close the reader.
     * @throws IOException if close fails.
     */
    @After
    public void closeFasta() throws IOException {
        fasta.close();
    }

    /**
     * Test get genome.
     */
    @Test
    public void testFastaGetGenome() {
        assertThat(fasta.getGenome()).isEqualTo("gi|561108321|ref|NC_018143.2|");
    }

    /**
     * Test read of size 0.
     */
    @Test
    public void readNext0() {
        assertThat(fasta.readNext(0)).isNotNull()
                .isInstanceOf(byte[].class);
    }

    /**
     * Test get header.
     */
    @Test
    public void testFastaGetHeader() {
        assertThat(fasta.getHeader()).isEqualTo(">gi|561108321|ref|"
                + "NC_018143.2| Mycobacterium tuberculosis H37Rv, complete genome");
    }

    /**
     * Test read next length parameter.
     */
    @Test
    public void testReadNextLength() {
        assertThat(fasta.readNext(500).length).isEqualTo(500);
    }

    /**
     * Test values in read next.
     * @throws UnsupportedEncodingException if encoding not found.
     */
    @Test
    public void testReadNextOnce() throws UnsupportedEncodingException {
        assertThat(new String(fasta.readNext(105), "UTF-8")).isEqualTo("GCCGAAATCGTGGACA"
                + "GGCACGGCGACGACCGGCGTACCCGGATCATCGCGGCCGACGGAGACGTCA"
                + "GCGACGAGGATTTGATCGCCCGCGAGGACGTCGTTGTC");
    }

    /**
     * Test read next twice.
     * @throws UnsupportedEncodingException if encoding not found.
     */
    @Test
    public void testReadNextMultiple() throws UnsupportedEncodingException {
        assertThat(new String(fasta.readNext(105), "UTF-8")).isEqualTo("GCCGAAATCGTGGACA"
                + "GGCACGGCGACGACCGGCGTACCCGGATCATCGCGGCCGACGGAGACGTCA"
                + "GCGACGAGGATTTGATCGCCCGCGAGGACGTCGTTGTC");
        assertThat(new String(fasta.readNext(221), "UTF-8")).isEqualTo("ACTATCACCGAAACG"
                + "GGATACGCCAAGCGCACCAATTTACGTCAGGCCGCTGCCCATCACCAGCAG"
                + "ACCGCCGACTACCTGCGGACCGTGCCGTCGTCGCACGACGCGATCCGCGAA"
                + "AGTCTGGACTCGCTGGGGCCTATTTTCAGTGAGCTCCGCGACACCGGGCGT"
                + "GAGCTGCTCGAGCTCAGAAAGCAGTGCTACCAGCAGCAAGCCGACAACCACGC");
    }

    /**
     * Test read next until end.
     */
    @Test
    public void testReadNextAll() {
        byte[] got;
        while (true) {
            got = fasta.readNext(256);
            assertThat(got.length).isGreaterThan(0);
            if (got.length < 256) {
                got = fasta.readNext(256);
                assertThat(got.length).isEqualTo(0);
                return;
            }
        }
    }

    /**
     * Test start reading at a specific index.
     * @throws UnsupportedEncodingException if encoding not found.
     */
    @Test
    public void testReadOfIndex() throws UnsupportedEncodingException {
        assertThat(fasta.read(105, 221)).isEqualTo(("ACTATCACCGAAACG"
                + "GGATACGCCAAGCGCACCAATTTACGTCAGGCCGCTGCCCATCACCAGCAG"
                + "ACCGCCGACTACCTGCGGACCGTGCCGTCGTCGCACGACGCGATCCGCGAA"
                + "AGTCTGGACTCGCTGGGGCCTATTTTCAGTGAGCTCCGCGACACCGGGCGT"
                + "GAGCTGCTCGAGCTCAGAAAGCAGTGCTACCAGCAGCAAGCCGACAACCACGC").getBytes("UTF-8"));
    }


}
