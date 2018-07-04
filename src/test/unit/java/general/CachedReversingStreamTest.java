package general;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

/**
 * Tests for the Cached Reversing Stream.
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class CachedReversingStreamTest {

    /**
     * Blocksize must be at least 1.
     */
    @Test
    public void illegalArgument() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        assertThatThrownBy(() -> new CachedReversingStream(byteStream, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * You can not give a null as outputstream.
     */
    @Test
    public void nullPointer() {
        assertThatThrownBy(() -> new CachedReversingStream(null, 10))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Test correctly reveres if it fits into one block.
     */
    @Test
    public void testFitsInBlock() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            CachedReversingStream reversingStream = new CachedReversingStream(byteStream, 20);

            reversingStream.write("abcdef".getBytes());
            reversingStream.close();

            assertThat(new String(byteStream.toByteArray())).isEqualTo("fedcba");
        } catch (IOException e) {
            fail("IO Exception occurred", e);
        }
    }

    /**
     * Test correctly reveres if it fits exactly into one block.
     */
    @Test
    public void exactlyAnBlock() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            CachedReversingStream reversingStream = new CachedReversingStream(byteStream, 5);

            reversingStream.write("abcdef".getBytes());
            reversingStream.close();

            assertThat(new String(byteStream.toByteArray())).isEqualTo("fedcba");
        } catch (IOException e) {
            fail("IO Exception occurred", e);
        }
    }

    /**
     * Test correctly reveres if it fits into two blocks.
     */
    @Test
    public void exactlyTwoBlocks() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            CachedReversingStream reversingStream = new CachedReversingStream(byteStream, 10);

            reversingStream.write("abcdefghij".getBytes());
            reversingStream.close();

            assertThat(new String(byteStream.toByteArray())).isEqualTo("jihgfedcba");
        } catch (IOException e) {
            fail("IO Exception occurred", e);
        }
    }

    /**
     * Test if it correctly writes chunk that are larger than the blocksize.
     */
    @Test
    public void writeLargeChunk() {
        String example = "ABCDEFGHIJKLMOPQRSTUVWXYZ 9876543210ZYXWVUTSRQPOMLKJIHGFEDCBA";
        String result = "ABCDEFGHIJKLMOPQRSTUVWXYZ0123456789 ZYXWVUTSRQPOMLKJIHGFEDCBA";
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            CachedReversingStream reversingStream = new CachedReversingStream(byteStream, 10);

            reversingStream.write(example.getBytes());
            reversingStream.close();

            assertThat(new String(byteStream.toByteArray())).isEqualTo(result);
        } catch (IOException e) {
            fail("IO Exception occurred", e);
        }
    }

    /**
     * Test that writing if you use the default blocksize is also succesfull.
     */
    @Test
    public void largeChunkDefaultBlocksize() {
        String example = "ABCDEFGHIJKLMOPQRSTUVWXYZ 9876543210ZYXWVUTSRQPOMLKJIHGFEDCBA";
        String result = "ABCDEFGHIJKLMOPQRSTUVWXYZ0123456789 ZYXWVUTSRQPOMLKJIHGFEDCBA";
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            CachedReversingStream reversingStream = new CachedReversingStream(byteStream);

            reversingStream.write(example.getBytes());
            reversingStream.close();

            assertThat(new String(byteStream.toByteArray())).isEqualTo(result);
        } catch (IOException e) {
            fail("IO Exception occurred", e);
        }
    }

}
