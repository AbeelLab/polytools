package general;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Class with utility method to check if a stream is an zipped InputStream.
 */
public class GZip {

    /**
     * number of bytes that will be read for the GZip-header in the function {@link #isGZipInputStream(InputStream)}.
     */
    private static final int GZIP_HEADER_READ_LENGTH = 8000;

    /**
     * Private constructor since it is an utility class.
     */
    private GZip() {

    }
    /**
     * Test whether a input stream looks like a GZip input.
     *
     * @param stream the input stream.
     * @return true if `stream` starts with a gzip signature
     * @throws IllegalArgumentException if `stream` cannot mark or reset the stream.
     */
    public static boolean isGZipInputStream(final InputStream stream) {
        /* this function was previously implemented in SamStreams.isGzippedSAMFile */
        if (!stream.markSupported()) {
            throw new IllegalArgumentException("isGZipInputStream() : Cannot test a "
                    + "stream that doesn't support marking.");
        }
        stream.mark(GZIP_HEADER_READ_LENGTH);
        
        try {
            final GZIPInputStream gUnzip = new GZIPInputStream(stream);
            //noinspection ResultOfMethodCallIgnored
            gUnzip.read();
            return true;
        } catch (final IOException ioe) {
            return false;
        } finally {
            try {
                stream.reset();
            } catch (final IOException ioe) {
                //noinspection ThrowFromFinallyBlock
                throw new IllegalStateException("isGZipInputStream(): Could not reset stream.");
            }
        }
    }

}
