package vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.index.tabix.TabixFormat;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFFileReader;
import lombok.Getter;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;

/**
 * This class can be used to parse and query the htsjdk tool.
 * It uses internally the htsjdk VCFFileReader and has some utility methods to make
 * it easier to work with the vcf file.
 *
 * @see htsjdk.variant.vcf.VCFFileReader
 */
public class VCF implements AutoCloseable {

    private static final String EXTENSION = ".tbi";

    @Getter
    private final VCFFileReader variantContexts;

    /**
     * Creates a new VCF object from the given vcf file.
     * It will also create a index file if no index file exists.
     *
     * @param fileName the vcf file name.
     * @throws IOException if something went wrong with creating the file index file.
     */
    public VCF(String fileName) throws IOException {
        this(new File(fileName));
    }

    /**
     * Creates a new VCF object from the given vcf file.
     * It will also create a index file if no index file exists.
     *
     * @param file the vcf file.
     * @throws IOException if something went wrong with creating the file index file.
     */
    public VCF(File file) throws IOException {
        this(file, createIndex(file));
    }

    /**
     * Creates a new VCF object from the given vcf and given index file.
     *
     * @param file      The VCF file.
     * @param indexFile The VCF index file.
     */
    public VCF(@NonNull File file, @NonNull File indexFile) {
        if (!file.isFile()) {
            throw new IllegalArgumentException("VCF file does not exist: " + file.getAbsolutePath());
        }
        if (!indexFile.isFile()) {
            throw new IllegalArgumentException("Index file does not exist: " + indexFile.getAbsolutePath());
        }

        this.variantContexts = new VCFFileReader(file, indexFile);
    }

    /**
     * Query the reader for a list of chromosome from begin to end.
     *
     * @param chromosome The chromosome
     * @param begin      The begin position.
     * @param end        The end position.
     * @return The iterator of the VariantContexts
     * @see htsjdk.variant.vcf.VCFFileReader
     * @see htsjdk.variant.variantcontext.VariantContext
     */
    public CloseableIterator<VariantContext> query(String chromosome, int begin, int end) {
        return this.variantContexts.query(chromosome, begin, end);
    }

    /**
     * Creates an index for a VCF file for use in the VCFFileReader.
     * If an index was already found it will return this file instead of creating a new one.
     * The resulting filename of the index file we be the same as the parameter file with the
     * extension ".tbi" appended at the end.
     *
     * @param file The filename of the vcf file. Not the index file!
     * @return The created index file.
     * @throws IllegalArgumentException if the file did not exist.
     * @throws IOException              if something went wrong with creating the file.
     * @throws SecurityException        if there where invalid permissions for creating the file.
     */
    public static File createIndex(String file) throws IOException {
        return createIndex(new File(file));
    }

    /**
     * Creates an index for a VCF file for use in the VCFFileReader.
     * If an index was already found it will return this file instead of creating a new one.
     * The resulting filename of the index file we be the same as the parameter file
     * with the extension ".tbi" appended at the end.
     *
     * @param file The vcf file. Not the index file!
     * @return The created index file.
     * @throws IllegalArgumentException if the file did not exist.
     * @throws IOException              if something went wrong with creating the file.
     * @throws SecurityException        if there where invalid permissions for creating the file.
     */
    public static File createIndex(File file) throws IOException {
        return createIndex(file, null, true);
    }

    /**
     * Creates an index for a VCF file for use in the VCFFileReader.
     * If an index was already found it will return this file instead of creating a new one.
     * The resulting filename of the index file we be the same as the parameter file
     * with the extension ".tbi" appended at the end.
     *
     * @param file      The vcf file. Not the index file!
     * @param indexName The file name for the vcf file index file. If this is null it will use
     *                  the vcf file name with the extension ".tbi"
     * @param overwrite If the old index file should be overwritten in case it exists.
     * @return The created index file.
     * @throws IllegalArgumentException if the file did not exist.
     * @throws IOException              if something went wrong with creating the file.
     * @throws SecurityException        if there where invalid permissions for creating the file.
     */
    public static File createIndex(@NonNull File file, String indexName, boolean overwrite) throws IOException {
        if (!file.isFile()) {
            throw new IllegalArgumentException("The VCF file does not exist: " + file.getAbsolutePath());
        }

        if (indexName == null) {
            indexName = file.getAbsolutePath() + EXTENSION;
        }

        File indexFile = new File(indexName);
        if (indexFile.exists() && !overwrite) {
            return indexFile;
        }
        if (!indexFile.exists() && !indexFile.createNewFile()) {
            throw new IOException("Could not create the file: " + file.getAbsolutePath());
        }
        IndexFactory.createTabixIndex(file, new VCFCodec(),
                TabixFormat.VCF,
                new VCFFileReader(file, false).getFileHeader().getSequenceDictionary()).write(indexFile);

        return indexFile;
    }

    /**
     * Close the  VCFFileReader.
     *
     * @throws IOException if file could not be closed by VCFFileReader
     */
    @Override
    public void close() throws IOException {
        variantContexts.close();
    }

}
