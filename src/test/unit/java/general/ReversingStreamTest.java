package general;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for the ReversingStream class.
 * Created by regiv on 29/05/2018.
 */
public class ReversingStreamTest {

    /**
     * Tests if declaring too big a buffer throws an exception.
     */
    @Test
    public void badSizeMaxTest() {
        try {
            new ReversingStream(new ByteArrayOutputStream(), Integer.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Not enough memory to writeAlt the reverse complement.", e.getMessage());
        }
    }

    /**
     * Tests if declaring a normal buffer does not throw an exception.
     */
    @Test
    public void goodSizeTest() {
        try {
            new ReversingStream(new ByteArrayOutputStream(), 500000);
        } catch (Exception e) {
            fail();
        }
    }
}
