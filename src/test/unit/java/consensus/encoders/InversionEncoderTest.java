package consensus.encoders;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for inversion encoder.
 */
@RunWith(JUnitParamsRunner.class)
public class InversionEncoderTest {

    /**
     * Test inverse ref.
     */
    @Test
    public void testEncodeRefBlock() {
        IupacEncoder en = new InversionEncoder(new IupacEncoder());
        try {
            byte[] bytes = en.encodeReferenceBytes("AGCTKYWSRMBDHVL".getBytes("UTF-8"));
            assertEquals("TCGAMRWSYKVHDBN", new String(bytes, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            fail("Should not throw exception inverting bytes.");
        }
    }

    /**
     * Test inversion of bases in variant.
     *
     * @param data     The test data.
     * @param expected The expected result.
     */
    @Test
    @Parameters({
            "A, T",
            "G, C",
            "C, G",
            "T, A",
            "R, Y",
            "Y, R",
            "S, S",
            "W, W",
            "K, M",
            "M, K",
            "B, V",
            "V, B",
            "D, H",
            "H, D",
            "N, N"
        }
    )
    public void testInverse(char data, char expected) {
        IupacEncoder en = new InversionEncoder(new IupacEncoder());
        assertEquals(expected, en.encode(new byte[]{(byte) data}));
    }
}
