package consensus.encoders;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The class that test the default methods of the IupacEncode interface.
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
@RunWith(JUnitParamsRunner.class)
@Ignore
public class EncoderTest {

    private IupacEncoder encoder;
    private VariantContext mockContext;
    private Allele mockReference;
    private Allele mockAlt;
    private byte[] byteReference;
    private byte[] byteAlt;

    /**
     * Set up all the fields.
     */
    @Before
    public void init() {
        encoder = new IupacEncoder();

        mockContext = mock(VariantContext.class);
        mockReference = mock(Allele.class);
        mockAlt = mock(Allele.class);

        byteReference = new byte[0];
        byteAlt = new byte[0];

        when(mockReference.getBases()).thenReturn(byteReference);
        when(mockAlt.getBases()).thenReturn(byteAlt);
    }

    /**
     * Test that from the combination of input chars the correct output char is given.
     *
     * @param reference The reference allele.
     * @param alt       The alternate allele.
     * @param result    The result allele.
     */
    @Test
    @Parameters({"A,A,A", "G,G,G", "T,T,T", "C,C,C",
            "A,G,R", "C,T,Y", "G,C,S", "A,T,W", "G,T,K", "A,C,M", "N,A,N",
            "B,B,B"})
    public void testToIuapc(char reference, char alt, char result) {
        assertThat((char) encoder.toIUPAC((byte) reference, (byte) alt)).isEqualTo(result);
        assertThat((char) encoder.toIUPAC((byte) alt, (byte) reference)).isEqualTo(result);
    }

    /**
     * Test if the correct iupac encoding is returned from the given variantcontext.
     *
     * @param reference The reference genome.
     * @param alternate The alternate genome.
     * @param af        The allele frequency.
     * @param result    The resulting sequence.
     */
    @Test
    @Parameters(
            {
                    "AA,CC,0.1,AA",
                    "AA,CC,0.9,CC",
                    "AC,CT,0.5,MY",
                    "ACA,CT,0.5,MY[A]",
                    "AC,CTA,0.5,MY(A)",
                    "RC,CTA,0.5,VY(A)",
            }
    )
    public void testEncoding(String reference, String alternate, double af, String result) {

        when(mockReference.getBases()).thenReturn(reference.getBytes());
        when(mockAlt.getBases()).thenReturn(alternate.getBytes());
        when(mockContext.getAttribute("AF")).thenReturn("" + af);

        List<Allele> list = new ArrayList<>();
        list.add(mockAlt);
        when(mockContext.getAlternateAlleles()).thenReturn(list);
        when(mockContext.getReference()).thenReturn(mockReference);

        // assertThat(new String(encoder.encode(mockContext).getBytes())).isEqualTo(result);
    }

    /**
     * Test that you get the correct alternate allele.
     */
    @Test
    public void getAlt() {
        List<Allele> list = new ArrayList<>();
        list.add(mockAlt);
        when(mockContext.getAlternateAlleles()).thenReturn(list);
        when(mockContext.getReference()).thenReturn(mockReference);

        // assertThat(encoder.getAlt(mockContext) == byteAlt).isTrue();
    }

    /**
     * Test that you get the reference allele back.
     */
    @Test
    public void getRef() {

        List<Allele> list = new ArrayList<>();
        when(mockContext.getAlternateAlleles()).thenReturn(list);
        when(mockContext.getReference()).thenReturn(mockReference);

        //assertThat(encoder.getAlt(mockContext) == byteReference).isTrue();
    }

}
