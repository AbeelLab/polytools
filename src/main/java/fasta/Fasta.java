package fasta;

import java.io.Closeable;

/**
 * Fasta file abstract class.
 * Abstract class for FastaSequence and FastaRandom.
 * Use these 2 classes appropriately to read from a fasta file fast.
 * Implementations consider the Fasta to have a 0 based index.
 */
public abstract class Fasta implements Closeable {
    private String fastaHeader;

    /**
     * Get the fasta.Fasta header at the top of the file.
     * This is a 1 line String, as by the Fasta description.
     *
     * @return the Fasta header.
     */
    public String getHeader() {
        return fastaHeader;
    }

    /**
     * Set the fasta header.
     *
     * @param fastaHeader the new header.
     */
    void setFastaHeader(String fastaHeader) {
        this.fastaHeader = fastaHeader;
    }

    /**
     * On finalize, call the close method to release the stream.
     *
     * @throws Throwable if close fails, but doesn't matter for finalize.
     */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * Gets the genome.
     * @return The genome string.
     */
    public String getGenome() {
        String header = getHeader();
        if (header.startsWith(">")) {
            header = header.substring(1);
        }
        String[] parts = header.split(" ", 2);
        return parts[0];
    }

    /**
     * Read a fasta file block by ID.
     * Blocks are separated by a white line in the fasta file.
     * @param blockId the block to read, 0 based index.
     * @return the block, as byte[].
     */
    public abstract byte[] readBlockId(int blockId);

    /**
     * Read the next 'length' bytes from where we were.
     * @param length the length to read.
     * @return the filled byte array, fitted to size.
     */
    public abstract byte[] readNext(int length);

    /**
     * Read the next 'length' bytes from a specific position.
     * @param startIndex the position to start at.
     * @param length the amount of bytes to read.
     * @return a filled byte array, fitted to size.
     */
    public abstract byte[] read(int startIndex, int length);
}
