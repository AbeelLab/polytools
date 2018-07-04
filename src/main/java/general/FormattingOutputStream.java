package general;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by regiv on 22/05/2018.
 * A wrapper for OutputStreams which inserts line breaks every certain amount of characters.
 */
public class FormattingOutputStream extends OutputStream {
    private static final byte[] ANSI_RESET = new byte[]{27, 91, 48, 109};
    private static final byte[] ANSI_RED = new byte[]{27, 91, 51, 49, 109};
    private static final byte[] ANSI_GREEN = new byte[]{27, 91, 51, 50, 109};
    private static final byte[] ANSI_YELLOW = new byte[]{27, 91, 51, 51, 109};
    private static final byte[] ANSI_BLUE = new byte[]{27, 91, 51, 52, 109};
    private static final byte[] ANSI_PURPLE = new byte[]{27, 91, 51, 53, 109};
    private static final byte[] ANSI_CYAN = new byte[]{27, 91, 51, 54, 109};
    private static final byte[] ANSI_WHITE = new byte[]{27, 91, 51, 55, 109};
    private static final byte[] ANSI_BLACK = new byte[]{27, 91, 51, 48, 109};


    private OutputStream stream;
    private int counter = 0;
    private int colorLevel;
    @Getter
    @Setter
    private int linebreakThreshold;
    @Getter
    private boolean formatting = false;

    /**
     * Creates a new FormattingOutputStream with a given OutputStream to write to and a line break threshold.
     * Formatting is disabled by default.
     *
     * @param stream             The stream to write to.
     * @param linebreakThreshold The amount of characters before a line break should be inserted.
     */
    public FormattingOutputStream(OutputStream stream, int linebreakThreshold) {
        this(stream, linebreakThreshold, 0);
    }

    /**
     * Creates a new FormattingOutputStream with a given OutputStream to write to and a line break threshold.
     * Formatting is disabled by default.
     *
     * @param stream             The stream to write to.
     * @param linebreakThreshold The amount of characters before a line break should be inserted.
     * @param color              Use colorLevel in stream.
     */
    public FormattingOutputStream(OutputStream stream, int linebreakThreshold, int color) {
        this.stream = stream;
        this.linebreakThreshold = linebreakThreshold;
        this.colorLevel = color;
    }

    /**
     * Writes byte to the OutputStream this class wraps.
     * If formatting is enabled, it will automatically insert line endings when the linebreak threshold is reached.
     *
     * @param b the byte to write
     * @throws IOException If the underlying OutputStream throws an exception
     */
    @Override
    public void write(int b) throws IOException {
        writeColor(b, colorLevel);
    }

    private void writeColor(int b, int color) throws IOException {
        if (formatting && counter == linebreakThreshold) {
            stream.write("\n".getBytes("UTF-8"));
            stream.flush();
            counter = 0;
        }
        boolean resetColor = false;
        if (formatting && color > 0) {
            switch (color) {
                case 1:
                    resetColor = color1(b);
                    break;
                case 2:
                    resetColor = color2(b);
                    break;
                default:
                    break;
            }
        }
        stream.write(b);
        if (resetColor) {
            stream.write(ANSI_RESET);
        }
        counter++;
    }

    /**
     * Write an int (byte) in color or not.
     * This only works if color level is set to 3.
     * If this is not the case, just use default write method.
     *
     * @param b the byte to write.
     * @throws IOException if write fails.
     */
    private void writeAsAlt(int b) throws IOException {
        if (colorLevel != 3) {
            write(b);
            return;
        }
        //color 3:
//        if (changeCol) {
        writeColor(b, 2);
//        } else {
//            writeColor(b, 0);
//        }
    }

    /**
     * Write an int (byte) in color or not.
     * This only works if color level is set to 3.
     * If this is not the case, just use default write method.
     *
     * @param b   bytes to write.
     * @param off start offset.
     * @param len length to write.
     * @throws IOException if writeAlt fails.
     */
    public void writeAlt(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else {
            if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else {
                if (len == 0) {
                    return;
                }
            }
        }
        for (int i = 0; i < len; i++) {
            writeAsAlt(b[off + i]);
        }
    }

    /**
     * Color 1, where only IUPAC bases are colors.
     *
     * @param b the int to write.
     * @return the boolean to reset color or not.
     * @throws IOException if write failed.
     */
    private boolean color1(int b) throws IOException {
        switch (b) {
            case 'M':
            case 'Y':
                stream.write(ANSI_RED);
                return true;
            case 'R':
            case 'W':
                stream.write(ANSI_PURPLE);
                return true;
            case 'S':
            case 'K':
                stream.write(ANSI_BLUE);
                return true;
            case 'B':
            case 'D':
            case 'H':
            case 'V':
                stream.write(ANSI_GREEN);
                return true;
            case 'N':
                stream.write(ANSI_YELLOW);
                return true;
            default:
                return false;
        }
    }

    /**
     * Color 2, where each base is a color.
     *
     * @param b the int to write.
     * @return the boolean to reset color or not.
     * @throws IOException if write failed.
     */
    private boolean color2(int b) throws IOException {
        switch (b) {
            case 'A':
                stream.write(ANSI_PURPLE);
                return true;
            case 'C':
                stream.write(ANSI_GREEN);
                return true;
            case 'G':
                stream.write(ANSI_CYAN);
                return true;
            case 'T':
                stream.write(ANSI_RED);
                return true;
            case 'M':
            case 'Y':
            case 'W':
            case 'R':
            case 'S':
            case 'K':
                stream.write(ANSI_YELLOW);
                return true;
            case 'B':
            case 'D':
            case 'H':
            case 'V':
                stream.write(ANSI_BLUE);
                return true;
            case 'N':
                stream.write(ANSI_WHITE);
                return true;
            default:
                stream.write(ANSI_RESET);
                return false;
        }
    }

    /**
     * Stops formatting and resets the line break counter.
     */
    public void stopFormatting() {
        formatting = false;
        counter = 0;
    }

    /**
     * Flushes the underlying OutputStream.
     *
     * @throws IOException throws an IOException if the underlying OutputStream throws an IOException.
     *                     /
     * @Override public void flush throws IOException(){
     * stream.flush();
     * }
     * <p>
     * /**
     * Starts formatting. Does not reset the line break counter.
     */
    public void startFormatting() {
        formatting = true;
        counter = 0;
    }

    /**
     * Resets the line break counter.
     */
    public void reset() {
        counter = 0;
    }

    /**
     * Closes the underlying OutputStream.
     *
     * @throws IOException If the underlying OutputStream throws an exception.
     */
    @Override
    public void close() throws IOException {
        stream.flush();
        if (this.stream != System.out) {
            this.stream.close();
        }
    }
}
