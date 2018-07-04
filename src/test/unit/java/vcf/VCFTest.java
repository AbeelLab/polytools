package vcf;

import htsjdk.samtools.util.CloseableIterator;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;

/**
 * The VCF test class.
 */
public class VCFTest {
    private static final String FOLDER = "src/test/resources/VCF/";

    /**
     * Test to see if you create an index of an already existing file without overwrite
     * the same file is returned.
     */
    @Test
    public void createIndexExistingFile() {
        try {
            File file = new File(FOLDER + "file1.vcf.tbi");
            assertThat(VCF.createIndex(FOLDER + "file1.vcf")
                    .getAbsoluteFile()).isEqualTo(file.getAbsoluteFile());
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    /**
     * Test create index if file does not exists.
     */
    @Test
    public void createIndexNotExistingIndexFile() {
        try {
            File file = new File(FOLDER + "file3.tbi");

            assertThat(file.exists()).isFalse();
            VCF.createIndex(new File(FOLDER + "file3.vcf"),
                    FOLDER + "file3.tbi", false);
            assertThat(file.exists()).isTrue();

            if (!file.delete()) {
                fail("Could not delete file");
            }
        } catch (IOException e) {
            fail("IOException occurred" + e.getMessage());
        }
    }

    /**
     * Test that you cannot create an index of a non existing
     * vcf file.
     */
    @Test
    public void createIndexNotExistingFile() {
        assertThatThrownBy(() -> VCF.createIndex(FOLDER + "NA1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * The argument can not be null.
     */
    @Test
    public void createIndexNullFile() {
        assertThatThrownBy(() -> VCF.createIndex((File) null))
                .isInstanceOf(NullPointerException.class);
    }

   /**
     * Test that an exception occurs when you try to create a VCF of an non existing vcf file.
     */
    @Test
    public void createVCFIllegalArgumentFile1() {
        assertThatThrownBy(() -> new VCF("NA2.file"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test that an exception occurs when you try to create a VCF of an non existing vcf file
     * and an non existing index file.
     */
    @Test
    public void createVCFIllegalArgumentFile2() {
        assertThatThrownBy(() -> new VCF(new File(FOLDER), new File(FOLDER)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test that an exception occurs when you try to create a VCF with null as arguments.
     * and an non existing index file.
     */
    @Test
    public void createVCFIllegalArgumentFile3() {
        assertThatThrownBy(() -> new VCF(null, null))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Test that an exception occurs when you try to create a VCF with an non existing index file.
     */
    @Test
    public void createVCFIllegalArgumentIndex1() {
        File file = new File(FOLDER + "file2.vcf");
        File index = new File(FOLDER + "NA3.vcf");
        assertThatThrownBy(() -> new VCF(file, index))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test that an exception occurs when you try to create a VCF with an non existing index file.
     */
    @Test
    public void createVCFIllegalArgumentIndex2() {
        File file = new File(FOLDER + "file2.vcf");
        File index = new File(FOLDER);
        assertThatThrownBy(() -> new VCF(file, index))
                .isInstanceOf(IllegalArgumentException.class);
    }


    /**
     * Test that an exception occurs when you pass null as the indexFile.
     */
    @Test
    public void createVCFIllegalArgumentIndex3() {
        File file = new File(FOLDER + "file2.vcf");
        assertThatThrownBy(() -> new VCF(file, null))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Test that if all parameters are correct no exceptions are thrown.
     */
    @Test
    public void createVCFSuccess() {
        try {
            assertThat(new VCF(FOLDER + "file1.vcf")
                    .getVariantContexts()).isNotNull();
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    /**
     * Test closing the VCF.
     */
    @Test
    public void testVCFClose() {
        try {
            new VCF(FOLDER + "file1.vcf").close();
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    /**
     * Test that no exception are thrown when you create a new file and overwrite it.
     * Also make sure that the file is indeed modified.
     */
    @Test
    public void createVCRSuccessOverwrite() {
        try {
            File file = new File(FOLDER + "file5.vcf");
            String index = FOLDER + "file5.vcf.tbi";
            File indexFile = new File(index);
            final long modified = indexFile.lastModified();
            assertThat(modified).isLessThan(VCF.createIndex(file, index, true).lastModified());
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    /**
     * Test that no exception are thrown when you create a new file and don't overwrite it.
     * Also make sure that the file is not modified.
     */
    @Test
    public void createVCRNoOverwrite() {
        try {
            File file = new File(FOLDER + "file4.vcf");
            String index = FOLDER + "file4.vcf.tbi";
            File indexFile = new File(index);
            final long modified = indexFile.lastModified();
            assertThat(modified).isEqualTo(VCF.createIndex(file, index, false).lastModified());
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }

    /**
     * Tests if you query the reader you get two variantcontexts.
     *
     */
    @Test
    public void query() {
        try {
            VCF vcf = new VCF(FOLDER + "file1.vcf");
            final CloseableIterator<VariantContext> query = vcf.query("gi|561108321|ref|NC_018143.2|", 233, 2000);
            assertThat(query.next()).isNotNull();
            assertThat(query.next()).isNotNull();
            assertThat(query.next()).isNull();
        } catch (IOException e) {
            fail("IOException occurred");
        }
    }
}
