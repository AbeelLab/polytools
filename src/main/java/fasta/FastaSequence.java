package fasta;

import general.GZip;
import logger.MultiLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

/**
 * Read a fasta file in sequence by having a large buffer.
 * Assumes that once index y is accessed, any index <= y does never have to be accessed.
 * Makes sure to only read letters from the file, thus skipping enters, spaces etc.
 * Instantly reads in the header on initialisation.
 * Works on both unix and windows line endings.
 */
public class FastaSequence extends Fasta {
    private final File file;
    private BufferedInputStream bufferedInputStream;
    private long lastReadLetterIndex;
    private int lastReadBlockIndex;

    /**
     * Create a sequential Fasta reader.
     * Reads from the given file name.
     * Automatically reads the header on initialisation.
     *
     * @param fileName the file name to read from.
     * @throws IOException if the file is not found or can not be read from.
     */
    public FastaSequence(String fileName) throws IOException {
        this(new File(fileName));
    }

    /**
     * Create a sequential Fasta reader.
     * Reads from the given file.
     * Automatically reads the header on initialisation.
     *
     * @param file the file to read from.
     * @throws IOException if the file is not found or can not be read from.
     */
    public FastaSequence(File file) throws IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("FASTA reference file does not exist: " + file.getAbsolutePath());
        }
        this.file = file;

        setFastaHeader(initBufferedInput());
    }

    private String initBufferedInput() throws IOException {
        lastReadLetterIndex = 0;
        lastReadBlockIndex = -1;
        bufferedInputStream = new BufferedInputStream(new FileInputStream(file), 16384);

        if (GZip.isGZipInputStream(bufferedInputStream)) {
            bufferedInputStream = new BufferedInputStream(new GZIPInputStream(bufferedInputStream));
        }


        int nextRead;
        StringBuilder sb = new StringBuilder();
        while ((nextRead = bufferedInputStream.read()) != -1) {
            if (nextRead == '\n' || nextRead == '\r') {
                break;
            }
            sb.append((char) nextRead);
        }
        return sb.toString();
    }

    private void reInitBufferedInput() {
        try {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                    bufferedInputStream = null;
                } catch (IOException ignore) {
                }
            }

            initBufferedInput();
            MultiLogger.get().println("!i Successfully created a new input in fasta!");
        } catch (IOException e) {
            MultiLogger.get().println("!f Failed to create a new input in fasta!");
            throw new IllegalStateException("Failed to read back in sequential fasta reader:" + e.getMessage());
        }
    }

    /**
     * Read a specified block, you can not read the same block twice,
     * and not access any block smaller than or equal to the block we are currently in.
     *
     * @param blockId the block id to read, 0 indexed.
     * @return the block, as byte[].
     */
    @Override
    public byte[] readBlockId(int blockId) {
        if (blockId <= lastReadBlockIndex) {
            reInitBufferedInput();
        }

        while (lastReadBlockIndex + 1 < blockId) {
            try {
                skipBlock();
            } catch (IOException ignore) {
                return new byte[0];
            }
        }


        LinkedList<byte[]> byteArrayList = new LinkedList<>();
        byte[] currentArray = new byte[1024];
        int readLetters;

        while (true) {
            readLetters = readLetters(currentArray, true);
            if (readLetters != -1) {
                // if we did not get -1, we need to stop reading.
                break;
            }
            //this means we could have read more.
            byteArrayList.add(currentArray);
            currentArray = new byte[1024];
        }

        // use a shit ton of arrays, but this is still way less overhead
        // than an arraylist or linkedlist of bytes or something.
        byte[] result = new byte[(byteArrayList.size() * 1024) + readLetters];
        int arrayIndex = 0;
        for (byte[] fullArray : byteArrayList) {
            //copy all byte[]s to the right place in the result
            System.arraycopy(fullArray, 0, result, arrayIndex * 1024, 1024);
            arrayIndex++;
        }
        //copy the last 'currentArray' into the result.
        System.arraycopy(currentArray, 0, result, arrayIndex * 1024, readLetters);
        return result;
    }

    /**
     * Read the next 'length' bytes, or until end of file.
     *
     * @param length the amount of bytes you want to get.
     * @return the filled byte array, only with letters.
     */
    public byte[] readNext(int length) {
        if (length <= 0) {
            return new byte[0];
        }
        byte[] ret = new byte[length];
        int read = readLetters(ret, false);
        if (read != length && read != -1) {
            byte[] newRet = new byte[read];
            System.arraycopy(ret, 0, newRet, 0, read);
            return newRet;
        }
        return ret;
    }

    /**
     * Read the next 'length' bytes from a given start index.
     * Assumes that the startIndex is later than or equal to the last read index.
     *
     * @param startIndex the index to start at.
     * @param length     the length of the block you want to get.
     * @return the bytes of the read letters.
     */
    public byte[] read(int startIndex, int length) {
        if (lastReadLetterIndex > startIndex) {
            //if we need to go back, start from the start.
            reInitBufferedInput();
        }
        if (startIndex > lastReadLetterIndex) {
            //skip stuff till at right index
            try {
                skip(startIndex);
            } catch (IOException e) {
                System.out.println("Could not skip till " + startIndex
                        + " because exception occurred");
                return new byte[0];
            }
        }
        return readNext(length);
    }

    /**
     * Skip until you are at some letter index place in the fasta file.
     *
     * @param untilLastReadLetterIndex the place to stop at.
     * @throws IOException if read failed.
     */
    private void skip(int untilLastReadLetterIndex) throws IOException {
        int nextRead;
        while ((nextRead = bufferedInputStream.read()) != -1) {
            if (nextRead >= 65 && nextRead <= 90) {
                lastReadLetterIndex++;
            }
            if (lastReadLetterIndex == untilLastReadLetterIndex) {
                break;
            }
        }
    }

    /**
     * Skip a single block.
     *
     * @throws IOException if reading throws IOException.
     */
    private void skipBlock() throws IOException {
        int nextRead;
        int lastRead = 0;
        while ((nextRead = bufferedInputStream.read()) != -1) {
            if (nextRead == '\r') {
                continue;
            }
            if (nextRead == '\n' && lastRead == '\n') {
                lastReadBlockIndex++;
                break;
            }
            if (nextRead >= 65 && nextRead <= 90) {
                lastReadLetterIndex++;
            }
            lastRead = nextRead;
        }
    }

    /**
     * Read the amount of bytes into the given byte array.
     * Prints errors if something went wrong.
     *
     * @param bytes       the byte array to fill.
     * @param stopAtBlock if true, stop when 2 enters are found.
     * @return the amount of letters read, or -1 if the whole
     * array was filled and we could have read more.
     */
    private int readLetters(byte[] bytes, boolean stopAtBlock) {
        int atIndex = 0;
        int lastRead = 0;
        int nextRead;


        try {
            while ((nextRead = bufferedInputStream.read()) != -1) {
                // skip '\r' characters, so that it works
                // for both unix and windows line endings.
                if (nextRead == '\r') {
                    continue;
                }

                // we reached the end of a block.
                // if we wanted to stop at block endings, do so.
                if (nextRead == '\n' && lastRead == '\n') {
                    lastReadBlockIndex++;
                    if (stopAtBlock) {
                        return atIndex;
                    }
                }

                // read the next character, but only if it is an actual letter.
                if (nextRead >= 65 && nextRead <= 90) {
                    bytes[atIndex] = (byte) nextRead;
                    lastReadLetterIndex++;
                    atIndex++;
                }

                // check if the array is full.
                if (atIndex == bytes.length) {
                    // returns -1 if we stopped because
                    // we filled the whole array.
                    // this will be useful sometimes.
                    return -1;
                }

                // set the last read the one we just did.
                lastRead = nextRead;
            }
        } catch (IOException e) {
            System.out.println("!w Tried to fill " + bytes.length
                    + " bytes, but only filled " + atIndex
                    + " bytes! (Because Exception: "
                    + e.getMessage() + ")");
        }
        return atIndex;
    }

    /**
     * Close the stream.
     *
     * @throws IOException if exception occurred in closing.
     */
    @Override
    public void close() throws IOException {
        this.bufferedInputStream.close();
    }
}
