package vcf.iterator;

import htsjdk.samtools.util.IOUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test the VCF iterator and iterator builder.
 */
public class VCFIteratorTest {
    private static final String FOLDER = "src/test/resources/VCF/";

    /**
     * Make an iterator.
     * @param fileName the file name to iterate over.
     * @return the iterator.
     * @throws IOException if file IO exception occurred.
     */
    private VCFIterator makeIterator(String fileName) throws IOException {
        return new VCFIteratorBuilder().open(FOLDER + fileName);
    }

    /**
     * Loop over iterator and count variants.
     * @param iterator the iterator.
     * @return the amount of variants.
     */
    private int countVariants(VCFIterator iterator) {
        int total = 0;
        while (iterator.hasNext()) {
            iterator.next();
            total++;
        }
        return total;
    }

    /**
     * Test for gzipped input file.
     * @param filepath the file path, no need to be gzipped.
     * @param nVariants the amount of expected variants.
     * @throws IOException if file IO failed.
     */
    private void testNumberOfVariantsGZippedInput(
            final String filepath, final int nVariants) throws IOException {
        File tmp =  new File(filepath);
        if (tmp.getName().endsWith(".vcf")) {
            tmp = File.createTempFile("tmp", ".vcf.gz");
            tmp.deleteOnExit();
            try (FileInputStream in = new FileInputStream(filepath);
                OutputStream out =  new GZIPOutputStream(new FileOutputStream(tmp))) {
                IOUtil.copyStream(in, out);
                out.flush();
            }
        }
        try (VCFIterator r = new VCFIteratorBuilder().open(tmp)) {
            assertThat(countVariants(r)).isEqualTo(nVariants);
        }
    }


    /**
     * Test amount of variants in file1.vcf.
     * @throws IOException if file IO failed.
     */
    @Test
    public void testNumberOfVariants1() throws IOException {
        VCFIterator iterator = makeIterator("file1.vcf");
        assertThat(countVariants(iterator)).isEqualTo(2575);
    }

    /**
     * Test variants in BCF.
     * @throws IOException if file IO failed.
     */
    @Test
    @Ignore
    public void testNumberOfVariantsBcf1() throws IOException {
        VCFIterator iterator = new VCFIteratorBuilder().open("src/test/resources/ConsensusGenerator/sample.bcf");
        assertThat(countVariants(iterator)).isEqualTo(2575);
    }

    /**
     * Test amount of variants in file6.vcf.
     * @throws IOException if file IO failed.
     */
    @Test
    public void testNumberOfVariants2() throws IOException {
        VCFIterator iterator = makeIterator("file6.vcf");
        assertThat(countVariants(iterator)).isEqualTo(6);
    }

    /**
     * Test amount of variants gzipped in file1.vcf.
     * @throws IOException if file IO failed.
     */
    @Test
    public void testNumberOfVariantsGZipped1() throws IOException {
        testNumberOfVariantsGZippedInput(FOLDER + "file1.vcf", 2575);
    }

    /**
     * Test amount of variants gzipped in file6.vcf.
     * @throws IOException if file IO failed.
     */
    @Test
    public void testNumberOfVariantsGZipped2() throws IOException {
        testNumberOfVariantsGZippedInput(FOLDER + "file6.vcf", 6);
    }

    /**
     * Test what happens if file is null.
     */
    @Test
    public void testOpenFileIsNull() {
        assertThatThrownBy(() -> new VCFIteratorBuilder()
                .open((File) null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Test what happens if path is null.
     */
    @Test
    public void testOpenStringIsNull() {
        assertThatThrownBy(() -> new VCFIteratorBuilder()
                .open((String) null)).isInstanceOf(NullPointerException.class);
    }

    /**
     * Test what happens if input stream is null.
     */
    @Test
    public void testOpenInputStreamIsNull() {
        assertThatThrownBy(() -> new VCFIteratorBuilder()
                .open((InputStream) null)).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test getting header.
     * @throws IOException if IO failed.
     */
    @Test
    public void testGetHeader1() throws IOException {
        VCFIterator iterator = makeIterator("file6.vcf");
        assertThat(iterator.getHeader().getContigLines().size()).isEqualTo(1);
    }

    /**
     * Test getting header.
     * @throws IOException if IO failed.
     */
    @Test
    public void testGetHeader2() throws IOException {
        VCFIterator iterator = makeIterator("file6.vcf");
        assertThat(iterator.getHeader().getContigLines().get(0).getID()).isEqualTo("sampleGen|");
    }

    /**
     * Test going over IUPAC.
     * @throws IOException if IO failed.
     */
    @Test
    public void testGoOverIUPAC() throws IOException {
        VCFIterator iterator = makeIterator("file7.vcf");
        assertThat(countVariants(iterator)).isEqualTo(6);
    }
}