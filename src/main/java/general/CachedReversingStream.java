package general;

import logger.MultiLogger;
import lombok.NonNull;

import java.io.*;
import java.util.LinkedList;

/**
 * This class is used to writeAlt large blocks to an underlying output stream.
 * It will writeAlt to temporary files if needed.
 * Once you flush this stream, everything that was written up until now will
 * be reversely written to the underlying output stream.
 */
public class CachedReversingStream extends OutputStream {

    private final OutputStream stream;
    private final int blockSize;
    private int counter;
    private ReversingStream reversingStream;
    private LinkedList<File> fileList;

    /**
     * Calculate a block size that fits in the current free memory.
     * The block size can be at most the value of {@link Integer#MAX_VALUE}.
     * Otherwise it will use 0.8 times the free space as block size.
     *
     * @return the block size.
     */
    private static int getBlockSize() {
        long free = Runtime.getRuntime().freeMemory();
        if (free * 8 / 10 > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return Math.toIntExact(free * 8 / 10);
    }

    /**
     * Creates a new stream with the underlying outputstream.
     *
     * @param stream The outputstream.
     */
    public CachedReversingStream(OutputStream stream) {
        this(stream, CachedReversingStream.getBlockSize());
    }


    /**
     * Creates a new stream with the underlying outputstream.
     * It will writeAlt to files of max block_size to avoid having to large
     * chunks in memory.
     *
     * @param stream    The underlying outputstream.
     * @param blockSize The block size.
     */
    public CachedReversingStream(@NonNull OutputStream stream, int blockSize) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Block size cannot by zero or negative");
        }
        this.stream = stream;
        this.blockSize = blockSize;
        this.counter = 0;
        this.fileList = new LinkedList<>();
    }

    /**
     * @inheritDocs
     */
    @Override
    public void write(int b) throws IOException {
        if (this.counter == blockSize || reversingStream == null) {
            this.newReversingStream();
            this.counter = 0;
        }

        reversingStream.write(b);

        this.counter++;
    }


    private void newReversingStream() throws IOException {
        if (reversingStream != null) {
            reversingStream.close();
        }

        String folder = "temp";
        String fileName = "stream" + fileList.size() + ".tmp";
        File root = new File(folder);
        File file = new File(root, fileName);
        file.deleteOnExit();

        if (!root.mkdir() || !file.createNewFile()) {
            MultiLogger.get().println("!w Could not create file, maybe the file already exists?");
        }

        fileList.addFirst(file);
        reversingStream = new ReversingStream(new FileOutputStream(file), this.blockSize);
    }

    /**
     * @inheritDocs
     */
    @Override
    public void close() throws IOException {
        flush();
        reversingStream.close();
        stream.close();
    }


    /**
     * @inheritDocs
     */
    @Override
    public void flush() throws IOException {
        reversingStream.flush();
        writeFromFiles();
        deleteFiles();
        stream.flush();
    }

    private void deleteFiles() {
        for (File file : fileList) {
            if (!file.delete()) {
                MultiLogger.get().println("!w Could not delete file:" + file.getAbsolutePath());
            }
        }
    }

    private void writeFromFiles() throws IOException {
        for (File file : fileList) {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                int read = fileInputStream.read();
                while (read != -1) {
                    stream.write(read);
                    read = fileInputStream.read();
                }
            } catch (IOException e) {
                fileInputStream.close();
                throw e;
            }

            fileInputStream.close();
        }
    }
}
