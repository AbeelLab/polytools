package consensus.encoders;

/**
 * This encoder inverses all variants it encodes and then passes it on to an underlying encoder.
 * Created by regiv on 29/05/2018.
 */
public class InversionEncoder extends IupacEncoder {

    private IupacEncoder encoder;

    /**
     * Creates a new encoder backed by a given encoder.
     *
     * @param encoder The backing encoder.
     */
    public InversionEncoder(IupacEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * Encodes a given array of nucleotides with the underlying encoder and then taking the complement.
     *
     * @param nucleotides The nucleotides to encode
     * @return The complement of the nucleotides, in IUPAC.
     */
    @Override
    public byte encode(byte[] nucleotides) {
        byte old = encoder.encode(nucleotides);

        return inverseIupac(old);
    }

    private byte inverseIupac(byte iupac) {
        switch (iupac) {
            case 'A':
                return 'T';
            case 'G':
                return 'C';
            case 'C':
                return 'G';
            case 'T':
                return 'A';
            case 'K':
                return 'M';
            case 'Y':
                return 'R';
            case 'W':
                return 'W';
            case 'S':
                return 'S';
            case 'R':
                return 'Y';
            case 'M':
                return 'K';
            case 'B':
                return 'V';
            case 'D':
                return 'H';
            case 'H':
                return 'D';
            case 'V':
                return 'B';
            default:
                return 'N';
        }
    }

    /**
     * This method encodes a byte array of an reference block.
     * It will switch A's with T's etc.
     *
     * @param referenceBlock The bytes to encode.
     * @return the encoded bytes.
     */
    @Override
    public byte[] encodeReferenceBytes(byte[] referenceBlock) {
        byte[] result = new byte[referenceBlock.length];
        for (int i = 0; i < referenceBlock.length; i++) {
            result[i] = inverseIupac(referenceBlock[i]);
        }
        return result;
    }
}