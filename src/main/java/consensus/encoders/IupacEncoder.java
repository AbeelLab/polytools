package consensus.encoders;

/**
 * Encode a context to IUPAC.
 */
public class IupacEncoder {
    /**
     * Encode method.
     *
     * @param nucleotides the context to encode.
     * @param
     * @return the byte array result.
     */
    public byte encode(byte[] nucleotides) {
        if (nucleotides.length == 0) {
            return '.';
        }

        byte result = nucleotides[0];
        for (int i = 1; i < nucleotides.length; i++) {
            result = toIUPAC(result, nucleotides[i]);
        }

        return result;
    }

    /**
     * This method encodes a fasta block or an reference nucleotide of an pylon vcf.
     * The method in the IupacEncoder will just return the original array.
     * Subclasses that implement this method should return the encoded array.
     * @param referenceBlock The byte array to encode.
     * @return the encoded byte array.
     */
    public byte[] encodeReferenceBytes(byte[] referenceBlock) {
        return referenceBlock;
    }

    /**
     * Get the Iupac encoded version of the reference + alternative.
     * As if they are heterozygous.
     *
     * @param variantContext the context.
     * @return the byte array of the new heterozygous thing.
     */

    /**
     * Get the reference bases.
     *
     * @param b              The byte.
     * @return true if it has an A.
     */
    private boolean hasA(byte b) {
        switch (b) {
            case 'A':
            case 'R':
            case 'W':
            case 'M':
            case 'D':
            case 'H':
            case 'V':
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether the IUAPC symbol encodes an C.
     *
     * @param b The byte.
     * @return true if it has an C.
     */
    private boolean hasC(byte b) {
        switch (b) {
            case 'C':
            case 'Y':
            case 'S':
            case 'M':
            case 'B':
            case 'H':
            case 'V':
            case 'N':
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether the IUAPC symbol encodes an T.
     *
     * @param b The byte.
     * @return true if it has an T.
     */
    private boolean hasT(byte b) {
        switch (b) {
            case 'T':
            case 'Y':
            case 'W':
            case 'K':
            case 'B':
            case 'D':
            case 'H':
            case 'N':
                return true;
            default:
                return false;
        }
    }


    /**
     * Checks whether the IUAPC symbol encodes an G.
     *
     * @param b The byte.
     * @return true if it has an G.
     */
    private boolean hasG(byte b) {
        switch (b) {
            case 'G':
            case 'R':
            case 'S':
            case 'K':
            case 'B':
            case 'D':
            case 'V':
            case 'N':
                return true;
            default:
                return false;
        }
    }

    /**
     * Convert 2 letters to their combined IUPAC encoding.
     *
     * @param let1 letter 1.
     * @param let2 letter 2.
     * @return their IUPAC letter.
     */
    byte toIUPAC(byte let1, byte let2) {
        boolean hasT = hasT(let1) || hasT(let2);
        boolean hasG = hasG(let1) || hasG(let2);
        boolean hasC = hasC(let1) || hasC(let2);
        boolean hasA = hasA(let1) || hasA(let2);
        return generateIupac(hasA, hasC, hasT, hasG);
    }

    private byte generateIupac(boolean hasA, boolean hasC, boolean hasT, boolean hasG) {
        int containsA = hasA ? 1 : 0;
        int containsC = hasC ? 1 : 0;
        int containsG = hasG ? 1 : 0;
        int containsT = hasT ? 1 : 0;
        int value = containsA + (containsC << 1) + (containsG << 2) + (containsT << 3);
        switch (value) {
            case 0b0001:
                return 'A';
            case 0b0010:
                return 'C';
            case 0b0100:
                return 'G';
            case 0b1000:
                return 'T';
            case 0b0011:
                return 'M';
            case 0b0101:
                return 'R';
            case 0b1001:
                return 'W';
            case 0b0110:
                return 'S';
            case 0b1010:
                return 'Y';
            case 0b1100:
                return 'K';
            case 0b0111:
                return 'V';
            case 0b1011:
                return 'H';
            case 0b1101:
                return 'D';
            case 0b1110:
                return 'B';
            default:
                return 'N';
        }
    }
}
