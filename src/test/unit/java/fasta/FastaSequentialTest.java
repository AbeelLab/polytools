package fasta;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

/**
 * Test for the sequential fasta reader.
 */
public class FastaSequentialTest extends FastaTest {
    /**
     * Make Fasta object.
     */
    @Before
    public void makeFasta() {
        try {
            fasta = new FastaSequence("src/test/resources/testFastaFile.fasta");
        } catch (IOException e) {
            fail("IOException when trying to read testFastaFile.fasta");
        } catch (Exception e) {
            fail("Runtime exception thrown " + e.getMessage());
        }
    }

    /**
     * Test name is null.
     */
    @Test
    public void testFastaFileNameNull() {
        assertThatThrownBy(() -> new FastaSequence((String) null))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Test name is non existing.
     */
    @Test
    public void testFastaFileNotExists() {
        assertThatThrownBy(() -> new FastaSequence("Some non existing file name"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test file is null.
     */
    @Test
    public void testFastaFileNull() {
        //noinspection ConstantConditions
        assertThatThrownBy(() -> new FastaSequence((File) null))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Test that sequential reader can not read backwards.
     */
    @Test
    public void readBackwardsBlock() {
        try {
            assertThat(new String(fasta.readBlockId(3), "UTF-8")).startsWith("GATCCTCCCGCCGGTGAC");
            assertThat(new String(fasta.readBlockId(2), "UTF-8")).startsWith("GCACTTCGCAATAACACCGCA");
        } catch (UnsupportedEncodingException e) {
            fail("Did not have UTF-8 encoding!");
        }
    }

    /**
     * Test exception on reading while reader is closed.
     * @throws IOException if closed failed.
     */
    @Test
    public void testClosedRead() throws IOException {
        fasta.close();
        assertThat(fasta.readNext(500).length).isEqualTo(0);
    }

    /**
     * Test exception on reading while reader is closed.
     * @throws IOException if closed failed.
     */
    @Test
    public void testClosedReadSkip() throws IOException {
        fasta.close();
        assertThat(fasta.read(300, 500)).hasSize(0);
    }

    /**
     * Test exception on reading while reader is closed.
     * @throws IOException if closed failed.
     */
    @Test
    public void testSkipBlockTooFar() throws IOException {
        fasta.close();
        assertThat(fasta.readBlockId(50)).hasSize(0);
    }

    /**
     * Checks whether a zipped fasta file works.
     */
    @Test
    public void zippedFasta() {
        try {
            FastaSequence sequence = new FastaSequence("src/test/resources/testFastaFile.fasta.gz");
            assertThat(sequence.getHeader()).isEqualTo(">gi|561108321|ref|"
                    + "NC_018143.2| Mycobacterium tuberculosis H37Rv, complete genome");
        } catch (IOException e) {
            fail("IOException when trying to read testFastaFile.fasta");
        } catch (Exception e) {
            fail("Runtime exception thrown " + e.getMessage());
        }

    }
}
