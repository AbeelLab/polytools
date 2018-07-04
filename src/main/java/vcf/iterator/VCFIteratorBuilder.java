package vcf.iterator;
/*
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Written by Pierre Lindenbaum.
 * Heavily modified by Koen du Buf
 */

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import general.GZip;
import htsjdk.samtools.util.AbstractIterator;
import htsjdk.samtools.util.CloserUtil;
import htsjdk.tribble.TribbleException;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.tribble.readers.PositionalBufferedStream;
import htsjdk.variant.bcf2.BCF2Codec;
import htsjdk.variant.bcf2.BCFVersion;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFHeader;
import lombok.Getter;

import java.io.*;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * A Class building {@link VCFIterator}.
 * <p>
 * Example:
 * <p>
 * <pre>
 * VCFIterator r = new VCFIteratorBuilder().open(System.in);
 * while (r.hasNext()) {
 *     System.out.println(r.next());
 * }
 * r.close();
 * </pre>
 *
 * @author Pierre Lindenbaum / @yokofakun
 */

public class VCFIteratorBuilder {
    /**
     * sizeof a BCF header (+ min/max version). Used when trying to detect when a streams starts with a bcf header
     */
    private static final int SIZEOF_BCF_HEADER = BCFVersion.MAGIC_HEADER_START.length + 2 * Byte.BYTES;




    /**
     * Creates a VCF iterator from an input stream.
     * It detects if the stream is a BCF stream or a GZipped stream.
     *
     * @param in inputstream
     * @return the VCFIterator
     * @throws IOException if something went wrong.
     */
    public VCFIterator open(final InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("input stream is null");
        }
        // wrap the input stream into a BufferedInputStream to reset/read a BCFHeader or a GZip
        // buffer must be large enough to contain the BCF header and/or GZip signature
        BufferedInputStream bufferedInput = new BufferedInputStream(in, 65536);
        // test for gzipped inputstream
        if (GZip.isGZipInputStream(bufferedInput)) {
            // this is a gzipped input stream, wrap it into GZIPInputStream
            // and re-wrap it into BufferedInputStream so we can test for the BCF header
            bufferedInput = new BufferedInputStream(new GZIPInputStream(bufferedInput), 65536);
        }

        // try to read a BCF header
        final BCFVersion bcfVersion = VCFIteratorBuilder.tryReadBCFVersion(bufferedInput);

        if (bcfVersion != null) {
            //this is BCF
            return new BCFInputStreamIterator(bufferedInput);
        } else {
            //this is VCF
            try {
                return new VCFReaderIterator(bufferedInput);
            } catch (TribbleException e) {
                throw new IllegalArgumentException("VCF file malformed: " + e.getMessage());
            }
        }
    }

    /**
     * Turn a file into an input stream and open it.
     *
     * @param file the file to open.
     * @return the VCFIterator.
     * @throws IOException an exception if file could not
     *                     be opened or open(Inputstream) throws an exception.
     */
    @SuppressFBWarnings("OBL")
    public VCFIterator open(final File file) throws IOException {
        return this.open(new FileInputStream(file));
    }

    /**
     * Turn a file into an input stream and open it.
     *
     * @param file the file to open.
     * @return the VCFIterator.
     * @throws IOException an exception if file could not
     *                     be opened or open(Inputstream) throws an exception.
     */
    @SuppressFBWarnings("OBL")
    public VCFIterator open(final String file) throws IOException {
        return this.open(new FileInputStream(new File(file)));
    }

    /**
     * implementation of VCFIterator, reading VCF.
     */
    public static class VCFReaderIterator
            extends AbstractIterator<VariantContext>
            implements VCFIterator {
        /**
         * delegate input stream.
         */
        private final InputStream inputStream;
        /**
         * VCF codec.
         */
        private final VCFCodec codec = new VCFCodec();
        /**
         * VCF header.
         */
        private final VCFHeader vcfHeader;
        /**
         * Last decoded line.
         */
        private String lastDecodedLine;
        /**
         * Last returned line.
         */
        @Getter
        private String lastReturnedLine;

        /**
         * Iterator over the lines of the VCF.
         */
        @Getter
        private final LineIterator lineIterator;

        /**
         * Constructor.
         *
         * @param bufferedStream the input.
         */
        VCFReaderIterator(final InputStream bufferedStream) {
            this.inputStream = bufferedStream;
            this.lineIterator = this.codec.makeSourceFromStream(this.inputStream);
            this.vcfHeader = (VCFHeader) this.codec.readActualHeader(this.lineIterator);
        }

        /**
         * Get the header.
         *
         * @return the header.
         */
        @Override
        public VCFHeader getHeader() {
            return this.vcfHeader;
        }

        /**
         * Advance iterator.
         *
         * @return the next context.
         */
        @Override
        protected VariantContext advance() {
            VariantContext next;
            do {
                if (!this.lineIterator.hasNext()) {
                    return null;
                }
                lastReturnedLine = lastDecodedLine;
                next = decodeNext();
                if (next != null) {
                    return next;
                }
            } while (true);
        }

        private VariantContext decodeNext() {
            //change IUPAC to the shitty things they want:
            //first get the thing after the 4th tab
            String line = this.lineIterator.next();
            this.lastDecodedLine = line;
            if (line.isEmpty()) {
                return null;
            }

            String[] parts = line.split("\t");

            if (parts.length < 8) {
                throw new IllegalArgumentException("VCF file malformed: found less than 8 columns");
            }

            //if the ref is IUPAC:
            if (containsIUPAC(parts[3])) {
                parts[7] += ";RIU=" + parts[3];
                char[] chars = new char[parts[3].length()];
                Arrays.fill(chars, 'N');
                parts[3] = new String(chars);
            }
            //if alt is IUPAC.
            if (containsIUPAC(parts[4])) {
                parts[7] += ";AIU=" + parts[4];
                char[] chars = new char[parts[4].length()];
                Arrays.fill(chars, 'N');
                parts[4] = "N";
            }

            return this.codec.decode(String.join("\t", parts));
        }

        /**
         * Check if something contains a IUPAC letter.
         *
         * @param s the string to check.
         * @return true if it contains an IUPAC letter.
         */
        private boolean containsIUPAC(String s) {
            s = s.toUpperCase();
            return s.contains("R") || s.contains("Y") || s.contains("S") || s.contains("W")
                    || s.contains("K") || s.contains("M") || s.contains("B") || s.contains("D")
                    || s.contains("H") || s.contains("V");
        }

        /**
         * Close iterator and input stream.
         */
        @Override
        public void close() {
            CloserUtil.close(this.lineIterator);
            CloserUtil.close(this.inputStream);
        }
    }

    /**
     * implementation of VCFIterator, reading BCF.
     */
    private static class BCFInputStreamIterator
            extends AbstractIterator<VariantContext>
            implements VCFIterator {
        /**
         * the underlying input stream.
         */
        private final PositionalBufferedStream inputStream;
        /**
         * BCF codec.
         */
        private final BCF2Codec codec = new BCF2Codec();
        /**
         * the VCF header.
         */
        private final VCFHeader vcfHeader;

        /**
         * Constructor.
         *
         * @param inputStream the input stream.
         */
        BCFInputStreamIterator(final InputStream inputStream) {
            this.inputStream = this.codec.makeSourceFromStream(inputStream);
            this.vcfHeader = (VCFHeader) this.codec.readHeader(this.inputStream).getHeaderValue();
        }

        /**
         * Get the header.
         *
         * @return the VCF Header.
         */
        @Override
        public VCFHeader getHeader() {
            return this.vcfHeader;
        }

        /**
         * Advance iterator.
         *
         * @return the next variant context.
         */
        @Override
        protected VariantContext advance() {
            return this.codec.isDone(this.inputStream)
                    ? null : this.codec.decode(this.inputStream);
        }

        /**
         * Close the input stream.
         */
        @Override
        public void close() {
            this.inputStream.close();
        }
    }

    // helper methods:

    /**
     * try to read a BCFVersion from an uncompressed BufferedInputStream.
     * The buffer must be large enough to contain {@link #SIZEOF_BCF_HEADER}
     *
     * @param uncompressedBufferedInput the uncompressed input stream
     * @return the BCFVersion if it can be decoded, or null if not found.
     * @throws IOException if the read from input stream failed.
     */
    private static BCFVersion tryReadBCFVersion(
            final BufferedInputStream uncompressedBufferedInput) throws IOException {
        uncompressedBufferedInput.mark(SIZEOF_BCF_HEADER);
        final BCFVersion bcfVersion = BCFVersion.readBCFVersion(uncompressedBufferedInput);
        uncompressedBufferedInput.reset();
        return bcfVersion;
    }
}