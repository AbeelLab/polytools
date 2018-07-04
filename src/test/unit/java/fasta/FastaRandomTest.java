package fasta;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

/**
 * Class for testing the Fasta class.
 */
public class FastaRandomTest extends FastaTest {
    /**
     * Make Fasta object.
     */
    @Before
    public void makeFasta() {
        try {
            fasta = new FastaRandom("src/test/resources/testFastaFile.fasta", true);
        } catch (IOException e) {
            fail("IOException when trying to read testFastaFile.fasta");
        } catch (Exception e) {
            fail("Runtime exception thrown " + e.getMessage());
        }
    }

    /**
     * Test counting blocks at start.
     */
    @Test
    public void testFastaBlockCount() {
        assertThat(((FastaRandom) fasta).amountOfBlocks()).isEqualTo(8);
    }

    /**
     * Test reading a block out of bounds.
     */
    @Test
    public void testFastaIndexOutOfBounds() {
        assertThatThrownBy(() -> fasta.readBlockId(12))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    /**
     * Test name is null.
     */
    @Test
    public void testFastaFileNameNull() {
        assertThatThrownBy(() -> new FastaRandom((String) null,
                false)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Test file is null.
     */
    @Test
    public void testFastaFileNull() {
        assertThatThrownBy(() -> new FastaRandom((File) null,
                false)).isInstanceOf(IOException.class);
    }

    /**
     * Close the stream, then seek.
     * @throws IOException if close failed.
     */
    @Test
    public void testCloseThenSeek() throws IOException {
        //wait for initial read to finish.
        fasta.readNext(1);
        fasta.close();
        assertThat(fasta.read(5000, 100)).isNull();
    }

    /**
     * Close the stream, then seek.
     */
    @Test
    public void testSeekAndSkip() {
        fasta.readNext(1);
        // skipping bigger than the file is:
        assertThat(fasta.read(15000000, 100)).isNull();
    }

    /**
     * Close the stream, then read.
     * @throws IOException if close failed.
     */
    @Test
    public void testCloseThenRead() throws IOException {
        //wait for initial read
        fasta.readNext(1);
        fasta.close();
        // skipping bigger than the file is:
        assertThat(fasta.readNext(1000).length).isEqualTo(0);
    }

    /**
     * Close the stream, then seek.
     */
    @Test
    public void testSeekAndSkip2() {
        fasta.readNext(1);
        // skipping bigger than the file is:
        assertThat(fasta.read(14000000, 100)).isNull();
    }

    /**
     * Close the stream, then seek.
     * @throws IOException if close failed.
     */
    @Test
    public void testCloseThenSeek2() throws IOException {
        //wait for initial read to finish.
        fasta.readNext(1);
        fasta.close();
        assertThat(fasta.readBlockId(6)).isNull();
    }
}
