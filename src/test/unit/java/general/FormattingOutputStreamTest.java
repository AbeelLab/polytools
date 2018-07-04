package general;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Class for testing the FormattingOuputStream.
 * Created by regiv on 22/05/2018.
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
public class FormattingOutputStreamTest {
    private FormattingOutputStream stream;
    private ByteArrayOutputStream output;

    /**
     * Sets up the streams.
     */
    @Before
    public void init() {
        output = new ByteArrayOutputStream();
        stream = new FormattingOutputStream(output, 1);
    }

    /**
     * Closes the streams.
     */
    @After
    public void tearDown() {
        try {
            stream.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Tests if the stream writes with line breaks.
     *
     * @throws IOException If something goes wrong with the underlying stream.
     */
    @Test
    public void write() throws IOException {
        stream.startFormatting();
        stream.write('a');
        stream.write('b');
        assertEquals(3, output.toByteArray().length);
    }

    /**
     * Tests if stopFormatting() stops formatting.
     */
    @Test
    public void stopFormatting() {
        stream.startFormatting();
        stream.stopFormatting();
        assertFalse(stream.isFormatting());
    }


    /**
     * Tests if startFormatting() starts formatting.
     */
    @Test
    public void startFormatting() {
        stream.stopFormatting();
        stream.startFormatting();
        assertTrue(stream.isFormatting());
    }


    /**
     * Tests if reset resets the line break counter.
     *
     * @throws IOException If the underlying stream does not work properly.
     */
    @Test
    public void reset() throws IOException {
        stream.startFormatting();
        stream.write('a');
        stream.reset();
        stream.write('b');
        assertEquals(2, output.toByteArray().length);
    }


    /**
     * Tests if setting the line break threshold works.
     *
     * @throws IOException If the underlying stream does not work properly.
     */
    @Test
    public void setLinebreakThreshold() throws IOException {
        stream.startFormatting();
        stream.setLinebreakThreshold(10);
        stream.write("aaabbb".getBytes());
        assertEquals(6, output.toByteArray().length);
    }

    /**
     * Tests if getting the line break threshold actually returns the right value.
     */
    @Test
    public void getLinebreakThreshold() {
        stream.setLinebreakThreshold(10);
        assertEquals(10, stream.getLinebreakThreshold());
    }

    /**
     * Tests if the formatting flag gets returned correctly if started.
     */
    @Test
    public void isFormattingPositive() {
        stream.startFormatting();
        assertTrue(stream.isFormatting());
    }

    /**
     * Tests if the formatting flag gets correctly if stopped.
     */
    @Test
    public void isFormattingNegative() {
        stream.stopFormatting();
        assertFalse(stream.isFormatting());
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat1() {
        try {
            stream = new FormattingOutputStream(output, 1, 1);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("AGGCTT".getBytes());
            assertEquals(6, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }

    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat2() {
        try {
            stream = new FormattingOutputStream(output, 1, 0);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("MYRWSKBDHVNAGCT".getBytes());
            assertEquals(16, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat3() {
        try {
            stream = new FormattingOutputStream(output, 1, 1);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("M".getBytes());
            assertEquals(10, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat4() {
        try {
            stream = new FormattingOutputStream(output, 1, 1);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("MY".getBytes());
            assertEquals(20, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat5() {
        try {
            stream = new FormattingOutputStream(output, 1, 1);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("MMM".getBytes());
            assertEquals(30, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat6() {
        try {
            stream = new FormattingOutputStream(output, 1, 1);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("MYRWSKBDHVNAGCT".getBytes());
            //11 letters * 10 + 4 + 1 linebreak
            assertEquals(115, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat7() {
        try {
            stream = new FormattingOutputStream(output, 1, 2);
            stream.startFormatting();
            stream.setLinebreakThreshold(10);
            stream.write("MYRWSKBDHVNAGCT".getBytes());
            //15 letters * 10 + 1 linebreak
            assertEquals(151, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat8() {
        try {
            stream = new FormattingOutputStream(output, 1, 2);
            stream.startFormatting();
            stream.setLinebreakThreshold(20);
            stream.write("MYRWSKBDHVNAGCT".getBytes());
            //15 letters * 10
            assertEquals(150, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }

    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat9() {
        try {
            stream = new FormattingOutputStream(output, 1, 2);
            stream.startFormatting();
            stream.setLinebreakThreshold(20);
            stream.write("MYRWSKBDHVNAGCTI".getBytes());
            //15 letters * 10 + 1 * 5
            assertEquals(155, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }


    /**
     * Tests color settings.
     */
    @Test
    public void testColorFormat10() {
        try {
            stream = new FormattingOutputStream(output, 1, 3);
            stream.startFormatting();
            stream.setLinebreakThreshold(20);
            stream.writeAlt("MYRWSKBDHVNAGCTI".getBytes(), 0, 16);
            //15 letters * 10 + 1 * 5
            assertEquals(155, output.toByteArray().length);
        } catch (IOException e) {
            fail("IOException");
        }
    }
}