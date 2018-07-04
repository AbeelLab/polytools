package general;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This stream reverses all input given to it, then writes it to the backing outputstream.
 * Take care that it only writes when flush is called.
 * This stream can only writeAlt in reverse if it fits into memory. If you want to writeAlt blocks that do not
 * fit in memory use {@link CachedReversingStream}
 * Created by regiv on 29/05/2018.
 */
public class ReversingStream extends OutputStream {
    private OutputStream stream;
    private byte[] buffer;
    private int counter = 0;
    public static final double MAX_MEMORY_PERCENTAGE = 0.8;

    /**
     * Creates a new reversing stream backed by a given outputstream, with a certain buffer size.
     *
     * @param stream The outputstream backing this stream.
     * @param size   The size of the buffer. Exceeding this amount before flushing causes an error.
     */
    public ReversingStream(OutputStream stream, int size) {
        this.stream = stream;
        double maxMemory = MAX_MEMORY_PERCENTAGE * Runtime.getRuntime().maxMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        if (size > freeMemory || size > maxMemory) {
            throw new IllegalArgumentException("Not enough memory to writeAlt the reverse complement.");
        }
        this.buffer = new byte[size];
    }

    private byte reverseBracket(byte b) {
        switch (b) {
            case (byte) '(':
                b = ')';
                break;
            case (byte) ')':
                b = '(';
                break;
            case (byte) '[':
                b = ']';
                break;
            case (byte) ']':
                b = '[';
                break;
            default:
                break;
        }
        return b;
    }

    /**
     * Writes a given byte to the buffer.
     *
     * @param b the byte to writeAlt.
     * @throws IOException If this stream's buffer is exceeded
     */
    public void write(byte b) throws IOException {
        b = reverseBracket(b);
        if (counter < buffer.length) {
            buffer[counter] = b;
            counter++;
        } else {
            throw new IOException("Reversion too large.");
            //TODO: Do better handling here.
        }

    }

    /**
     * Writes a given integer as a byte to the buffer.
     *
     * @param b The int to use as byte.
     * @throws IOException If this stream's buffer is exceeded
     */
    @Override
    public void write(int b) throws IOException {
        write((byte) b);
    }

    /**
     * Writes the buffer to the stream. This is the only way to writeAlt with this stream.
     *
     * @throws IOException If the underlying stream throws an exception.
     */
    @Override
    public void flush() throws IOException {
        for (int i = counter - 1; i >= 0; i--) {
            stream.write(buffer[i]);
        }
        counter = 0;
        stream.flush();
    }

    /**
     * Flushes, then closes the stream.
     *
     * @throws IOException if the underlying stream throws an exception.
     */
    @Override
    public void close() throws IOException {
        flush();
        stream.close();
    }
}
