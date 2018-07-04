package fasta;

import general.DynamicBoolean;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * Basically a wrapper for a fasta.Fasta file.
 * You can use getBlock(int index) to quickly get a block in the fasta file.
 * Does not keep any blocks into memory, but points to a random access file.
 * <p>
 * For the record: FindBugs needs this to be a final class because
 * it is not allowed to start a thread in a constructor if a class is not final.
 */
public final class FastaRandom extends Fasta {
    /**
     * Lock used to prevent deadlock or consistency issues.
     */
    private final DynamicBoolean initialReadDone =
            new DynamicBoolean(false);

    /**
     * The amount of blocks found in the fasta file.
     */
    private int amountOfBlocks;

    /**
     * The start position of all blocks found.
     */
    private long[] blockStartPos;

    /**
     * The pointer position of the 'index * storePointerInterval' letter in the fasta.
     */
    private long[] intervalStartPos;

    /**
     * The interval where the reader should store the file pointer to a given base.
     */
    private final int storePointerInterval;

    /**
     * The random access reader.
     */
    private RandomAccessFile fileAccess;

    /**
     * Create a fasta.Fasta instance from a file name put into new File(fileName).
     * Option for ASync initial read is included in parameter.
     *
     * @param fileName  the file name to create the fasta.Fasta instance by.
     * @param readASync if true, the initial read is done async.
     * @throws IOException          when the file is not found or can not be read.
     * @throws NullPointerException when the filename was null.
     */
    public FastaRandom(String fileName, boolean readASync) throws IOException {
        this(new File(fileName), readASync);
    }

    /**
     * Create a fasta.Fasta instance directly from a given file.
     * Option for ASync initial read is included in parameter.
     *
     * @param file      the file to create the fasta.Fasta instance by.
     * @param readASync if true, the initial read is done async.
     * @throws IOException          when the file is not found or can not be read.
     * @throws NullPointerException when the file was null.
     */
    public FastaRandom(File file, boolean readASync) throws IOException {
        storePointerInterval = 1024;

        if (readASync) {
            new Thread(() -> {
                try {
                    initialRead(file);
                } catch (IOException e) {
                    System.err.println("Error on ASync reading of fasta.Fasta file.");
                }
            }).start();
        } else {
            initialRead(file);
        }
    }

    /**
     * Wait until the initial read is done.
     */
    private void waitForInitialRead() {
        synchronized (initialReadDone) {
            //need this to prevent deadlock
            while (!initialReadDone.get()) {
                try {
                    initialReadDone.wait();
                } catch (InterruptedException ignore) {
                }
            }

        }
    }

    /**
     * Return the amount of blocks that the fasta.Fasta
     * instance contains (a file pointer to).
     *
     * @return the number of blocks.
     */
    public int amountOfBlocks() {
        waitForInitialRead();
        return amountOfBlocks;
    }

    /**
     * Get the fasta.Fasta header at the top of the file.
     * This is a 1 line String, as by the fasta.Fasta description.
     *
     * @return the fasta.Fasta header.
     */
    public String getHeader() {
        waitForInitialRead();
        return super.getHeader();
    }

    /**
     * Read the next 'length' bytes, or until end of file.
     *
     * @param length the amount of bytes to read next.
     * @return the filled byte array, fitted to actual read size.
     */
    @Override
    public byte[] readNext(int length) {
        waitForInitialRead();
        if (length <= 0) {
            return new byte[0];
        }
        byte[] ret = new byte[length];
        int read = readLetters(ret);
        if (read != length && read != -1) {
            byte[] newRet = new byte[read];
            System.arraycopy(ret, 0, newRet, 0, read);
            return newRet;
        }
        return ret;
    }

    /**
     * Read the next length bytes from the given start index.
     * Return null on IOException.
     *
     * @param startIndex the index to start reading.
     * @param length     the length to read.
     * @return the filled byte array, fitted to size.
     */
    @Override
    public byte[] read(int startIndex, int length) {
        waitForInitialRead();
        int intervalBlockIndex = startIndex / storePointerInterval;
        if (intervalBlockIndex >= intervalStartPos.length) {
            intervalBlockIndex = intervalStartPos.length - 1;
        }

        int amountOfBasesFurther = startIndex - (intervalBlockIndex * storePointerInterval);
        long pointerPosition = intervalStartPos[intervalBlockIndex];
        try {
            fileAccess.seek(pointerPosition);
            boolean skipped = skip(amountOfBasesFurther);
            if (!skipped) {
                return null;
            }
            return readNext(length);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Skip some amount of bases.
     *
     * @param amountOfBases the amount of bases to skip.
     * @return true if no exception, false if failed.
     */
    private boolean skip(int amountOfBases) {
        int nextRead;
        int amountRead = 0;

        try {
            while (amountRead != amountOfBases) {
                nextRead = fileAccess.readByte();
                // read the next character, but only if it is an actual letter.
                if (nextRead >= 65 && nextRead <= 90) {
                    amountRead++;
                }
            }
        } catch (IOException e) {
            return false;
        }
        System.out.println(amountRead);
        return true;
    }

    /**
     * Get a block from the fasta.Fasta file.
     * Return null if IOException occurred.
     *
     * @param index the block index, starting, of course, at 0.
     * @return the block, a DNA sequence as a String.
     * @throws IndexOutOfBoundsException when index < 0 or index >= amountOfBlocks.
     */
    public byte[] readBlockId(int index) {
        waitForInitialRead();
        if (index < 0 || index >= amountOfBlocks) {
            throw new IndexOutOfBoundsException("Index '" + index
                    + "' out of bounds: amount of amountOfBlocks was " + amountOfBlocks);
        }
        try {
            fileAccess.seek(blockStartPos[index]);
            return readBlockAtPointer();
        } catch (IOException ignore) {
        }
        return null;
    }

    /**
     * Initial file read.
     * Used to index and count blocks.
     *
     * @throws IOException on error reading from file or finding it.
     */
    private void initialRead(File fastaFile) throws IOException {
        if (fastaFile == null || !fastaFile.exists() || !fastaFile.isFile()) {
            throw new IOException("Not a valid Fasta File: "
                    + (fastaFile != null ? fastaFile.getAbsolutePath() : "null"));
        }

        this.amountOfBlocks = 0;
        this.blockStartPos = new long[5];
        this.fileAccess = new RandomAccessFile(fastaFile, "rw");

        //assume first line is header, skip empty lines?
        String currentLine = fileAccess.readLine();
        setFastaHeader(currentLine);

        ArrayList<Long> arrayListInterval = new ArrayList<>();
        boolean lastLineEmpty = true;
        long lettersRead = 0;

        long pointerBeforeRead = fileAccess.getFilePointer();
        arrayListInterval.add(pointerBeforeRead);
        while ((currentLine = fileAccess.readLine()) != null) {
            if (currentLine.isEmpty() || currentLine.startsWith(";")
                    || currentLine.startsWith(">") || currentLine.charAt(0) < 65) {
                //found empty or comment line
                lastLineEmpty = true;
                pointerBeforeRead = fileAccess.getFilePointer();
                continue;
            }

            if (lastLineEmpty) {
                //full line, while last line was empty.
                lastLineEmpty = false;
                //register the block in the block index array
                foundNewBlock(pointerBeforeRead);
            }

            //register the position in the interval index array.
            if ((lettersRead % storePointerInterval)
                    > ((lettersRead + currentLine.length()) % storePointerInterval)) {
                //if this line enters a new 'interval block'.
                long amountOfLettersFurther = storePointerInterval - (lettersRead % storePointerInterval);
                arrayListInterval.add(pointerBeforeRead + amountOfLettersFurther);
            }
            lettersRead += currentLine.length();
            pointerBeforeRead = fileAccess.getFilePointer();
        }

        this.intervalStartPos = new long[arrayListInterval.size()];
        for (int i = 0; i < arrayListInterval.size(); i++) {
            intervalStartPos[i] = arrayListInterval.get(i);
        }

        fileAccess.seek(intervalStartPos[0]);
        synchronized (initialReadDone) {
            initialReadDone.set(true);
            initialReadDone.notifyAll();
        }
    }

    /**
     * When a new block is found.
     * Add one the amount of blocks.
     * and make sure to store the file pointer.
     *
     * @param startPosInFile the block location in file.
     */
    private void foundNewBlock(long startPosInFile) {
        if (blockStartPos.length <= amountOfBlocks) {
            //increase array size by 10 whenever it is too small
            long[] newIntArr = new long[amountOfBlocks + 10];
            System.arraycopy(this.blockStartPos, 0, newIntArr,
                    0, this.blockStartPos.length);
            this.blockStartPos = newIntArr;
        }
        this.blockStartPos[amountOfBlocks] = startPosInFile;
        this.amountOfBlocks++;
    }

    /**
     * Read the next block.
     * Starting at the file pointer location in the fileAccess.
     *
     * @return the block at the pointer location, without enters.
     * @throws IOException on file read error.
     */
    private byte[] readBlockAtPointer() throws IOException {
        //set the file pointer first!
        StringBuilder block = new StringBuilder();
        String currentLine;
        while ((currentLine = fileAccess.readLine()) != null) {
            if (currentLine.isEmpty()) {
                break;
            }
            block.append(currentLine);
        }

        return block.toString().getBytes("UTF-8");
    }

    /**
     * Close the randomAccessFile.
     *
     * @throws IOException on randomAccessFile close exception.
     */
    @Override
    public void close() throws IOException {
        if (this.fileAccess == null) {
            return;
        }
        this.fileAccess.close();
    }

    /**
     * Read the letters into the given byte array.
     * Return the amount of letters read, or -1 if the whole array got filled.
     *
     * @param bytes the bytes to read into.
     * @return the amount of letters read, or -1 if whole array got filled.
     */
    private int readLetters(byte[] bytes) {
        int atIndex = 0;
        int nextRead;

        try {
            while (true) {
                nextRead = fileAccess.readByte();
                // read the next character, but only if it is an actual letter.
                if (nextRead >= 65 && nextRead <= 90) {
                    bytes[atIndex] = (byte) nextRead;
                    atIndex++;
                }

                // check if the array is full.
                if (atIndex == bytes.length) {
                    // returns -1 if we stopped because
                    // we filled the whole array.
                    // this will be useful sometimes.
                    return -1;
                }
            }
        } catch (EOFException e) {
            return atIndex;
        } catch (IOException e) {
            System.out.println("!w Tried to fill " + bytes.length
                    + " bytes, but only filled " + atIndex
                    + " bytes! (Because Exception: "
                    + e.getMessage() + ")");
        }
        return atIndex;
    }
}
