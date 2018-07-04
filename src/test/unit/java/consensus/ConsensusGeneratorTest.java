package consensus;

import cli.CommandRegion;
import cli.Region;
import consensus.encoders.IupacEncoder;
import consensus.filters.FilterInclude;
import consensus.filters.FilterInverse;
import consensus.filters.FilterQuality;
import consensus.samplers.AlleleFrequencySampler;
import consensus.samplers.SimpleSampler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fasta.FastaSequence;
import htsjdk.variant.variantcontext.filter.CompoundFilter;
import htsjdk.variant.variantcontext.filter.PassingVariantFilter;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import vcf.iterator.VCFIterator;
import vcf.iterator.VCFIteratorBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * These class contains tests to see if the correct
 * output is given from a few standard files.
 */
@SuppressFBWarnings("DM_DEFAULT_ENCODING")
@RunWith(JUnitParamsRunner.class)
public class ConsensusGeneratorTest {
    private static final String FOLDER = "src/test/resources/ConsensusGenerator";

    private ConsensusGenerator generator;
    private FastaSequence sequence = null;
    private VCFIterator vcf = null;
    private File fasta = null;

    /**
     * Initialize the fields.
     * fails if files could not be opened.
     */
    @Before
    public void init() {
        try {
            fasta = new File(FOLDER, "sample.fasta");
            sequence = new FastaSequence(fasta);
            vcf = new VCFIteratorBuilder().open(FOLDER + "/sample.vcf");
            generator = new ConsensusGenerator(sequence, FOLDER
                    + "/sample.vcf", vcf, new IupacEncoder(), new AlleleFrequencySampler(0.0, 0.9));
        } catch (IOException e) {
            fail("File could not be opened");
        }
    }

    /**
     * Close the streams.
     *
     * @throws IOException If the streams don't feel like being closed
     */
    @After
    public void tearDown() throws IOException {
        try {
            sequence.close();
        } catch (IOException e) {

        } finally {
            vcf.close();
        }
    }

    /**
     * This test if the correct sequence is written to the stream when starting at index 1.
     */
    @Test
    public void testStartAt1() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, 30),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAGCCCCAGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(30);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test if the correct sequence is written to the stream when starting at index 2.
     */
    @Test
    public void testStartAt2() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(2, 30),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TAGCCCCAGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(29);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test if the correct sequence is written to the stream when starting at index 3.
     */
    @Test
    public void testStartAt3() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(3, 30),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("AGCCCCAGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(28);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


    /**
     * This test if the correct sequence is written to the stream when starting at index 7.
     */
    @Test
    public void testStartAt7() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(7, 30),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("CCAGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(24);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test if the correct sequence is written to the stream when starting at index 15.
     */
    @Test
    public void testStartAt15() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(15, 30),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("CGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(16);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test if the correct sequence is written to the stream when the blockSize is smaller than the whole sequence.
     */
    @Test
    public void testStartAtSmallBlockSize() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, 32),
                    new CompoundFilter(false), System.err);
            assertThat(new String(stream.toByteArray())).isEqualTo("TTAGCCCCAGACCCCGGTTCAGGCTTCACCAC");
            assertThat(stream.toByteArray().length).isEqualTo(32);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This test if the correct sequence is written to the stream when not all references & variations are of length 1.
     */
    @Test
    public void testReplaceOfLongerLength() {
        //TTAGCCCAAGACCCCGGTTCAGGCTTCACC
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VCFIterator i = new VCFIteratorBuilder().open(new File(FOLDER, "sample2.vcf"));
            generator = new ConsensusGenerator(sequence, "sample2.vcf", i,
                    new IupacEncoder(), new AlleleFrequencySampler(0.0, 0.9));
            generator.writeNoHeader(stream, new CommandRegion(1, 32), new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAGCCCATGACCCCGGTTCAGGCTTCACCAC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(32);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test if only the qualities in the given range are included in the result.
     */
    @Test
    public void testQualityFilter() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, 30),
                    new FilterQuality(160, 190), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTGGCCCCTGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(30);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test if only the qualities in the given range are included in the result.
     */
    @Test
    public void testQualityFilterTurnMinMax() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, 30),
                    new FilterQuality(190, 160), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTGGCCCCTGACCCCGGTTCAGGCTTCACC".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(30);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test if all the variants the contain pass or del are included in the result.
     */
    @Test
    public void testVCFFilterMatchAny1() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
//            Filter filter = new FilterInclude(Arrays.asList("PASS", "Del"), false);
            CompoundFilter filter = new CompoundFilter(false);
            filter.add(new PassingVariantFilter());
            filter.add(new FilterInclude(Arrays.asList("PASS", "Del"), false));
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAGCCGATG".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(10);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests if all the variant that contains pass are included in the results.
     */
    @Test
    public void testVCFFilterMatchAny2() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
//            Filter filter = new FilterInclude(Collections.singletonList("PASS"), false);
            generator.writeNoHeader(stream, new CommandRegion(1, 10),
                    new PassingVariantFilter(), System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAACCGATG".getBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test if only the variants that contains a del filter are included in the results.
     */
    @Test
    public void testVCFFilterMatchAny3() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VariantContextFilter filter = new FilterInclude(Collections.singletonList("Del"), false);
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTGGCCGATG".getBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test if only the variants that contains a del, pass or amb filter are included in the results.
     */
    @Test
    public void testVCFFilterMatchAny4() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VariantContextFilter filter = new FilterInclude(Arrays.asList("PASS", "Del", "Amb"), false);
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAGCCCAAG".getBytes());
            assertThat(stream.toByteArray().length).isEqualTo(10);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests if only the variants that contains both a amb and a lowcov are included in the results.
     */
    @Test
    public void testVCFFilterMatchAll() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VariantContextFilter filter = new FilterInclude(Arrays.asList("Amb", "Lowcov"), true);
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTGACCGAAG".getBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests if only the variants that contains a pass are included in the reults.
     */
    @Test
    public void testVCFFilterMatchAllPass() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VariantContextFilter filter = new FilterInclude(Collections.singletonList("Pass"), true);
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTAACCGATG".getBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests whether the inverse filter works.
     */
    @Test
    public void testVCFFilterInverse() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VariantContextFilter filter = new FilterInverse(new FilterInclude(Collections.singletonList("Pass"), true));
            generator.writeNoHeader(stream, new CommandRegion(1, 10), filter, System.err);
            assertThat(stream.toByteArray()).isEqualTo("TTGGCCCCAG".getBytes());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests if an empty region does not throw an exception.
     */
    @Test
    public void testForEmptyRegion() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(15, 39),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).hasSize(40 - 15);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testEnd() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(200, 230),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).hasSize(11);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }


    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testOutOfBounds() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(220, 230),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).isEmpty();
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testHeaderWriteStartEndUnknown() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.write(stream, new CommandRegion(10, -1),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toString()).startsWith(">sampleGen|10-210|");
            assertThat(stream.toString().replaceAll("\r", "")).hasSize(220);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testHeaderWriteStartEnd() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.write(stream, new CommandRegion(20, 100), new CompoundFilter(false), System.err);
            assertThat(stream.toString()).startsWith(">sampleGen|20-100|");
            assertThat(stream.toString().replaceAll("\r", "")).hasSize(100);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testOutOfBounds1BlockSize() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(200, 40_000),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).hasSize(11);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }


    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testOutOfBounds2BlockSize() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(200, 80_000),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).hasSize(11);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testOutOfBoundsLargeValue() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, 0xffffff),
                    new CompoundFilter(false), System.err);
            stream.writeTo(System.out);
            assertThat(stream.toByteArray()).hasSize(210);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests if the index is out of bounds it still succeeds.
     */
    @Test
    public void testOutOfBoundsMaxValue() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            generator.writeNoHeader(stream, new CommandRegion(1, Integer.MAX_VALUE),
                    new CompoundFilter(false), System.err);
            assertThat(stream.toByteArray()).hasSize(210);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Test the region case vcf file.
     *
     * @param start    the start of region.
     * @param end      the end of region.
     * @param shouldBe what it should be as a string.
     */
    @Test
    @Parameters({"1, 11, ..AG..C[A]A..", "45, 60, .....A......T[RC]."})
    public void testRegionCasesVcf(int start, int end, String shouldBe) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            VCFIterator vcfIterator = new VCFIteratorBuilder().open(new File(FOLDER, "regionCases.vcf"));
            generator = new ConsensusGenerator(null, "regionCases.vcf",
                    vcfIterator, new IupacEncoder(), new AlleleFrequencySampler(0.25, 0.75));
            generator.writeNoHeader(stream, new CommandRegion(start, end),
                    new CompoundFilter(false), System.err);
            String result = new String(stream.toByteArray(), "UTF-8");
            assertThat(result).isEqualTo(shouldBe);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Test if overlapping regions still work correctly.
     *
     * @param regions The regions
     * @param result  The result
     */
    @Test
    @Parameters(method = "provideRegions")
    public void testOverlappingRegions(Region[] regions, String result) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            //Always take alternate to make testing easier.
            generator = new ConsensusGenerator(this.sequence, FOLDER + "/sample.vcf",
                    this.vcf, new IupacEncoder(), new AlleleFrequencySampler(0, 0));
            for (Region region : regions) {
                generator.writeNoHeader(stream, region, new CompoundFilter(false), System.err);
                stream.write(' ');
            }
            assertThat(new String(stream.toByteArray())).isEqualTo(result);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    @SuppressWarnings("checkstyle:Indentation")
    private Object[] provideRegions() {
        return new Object[]{
                new Object[]{getRegions(1, 7, 8, 14), "TTAGCCC CAGACCC "},
                new Object[]{getRegions(1, 7, 2, 6), "TTAGCCC TAGCC "},
                new Object[]{getRegions(1, 7, 8, 14, 1, 7), "TTAGCCC CAGACCC TTAGCCC "},
                new Object[]{getRegions(8, 14, 1, 10), "CAGACCC TTAGCCCCAG "},
                new Object[]{getRegions(1, 10, 8, 14), "TTAGCCCCAG CAGACCC "}
        };
    }

    private Region[] getRegions(int... args) {
        Region[] regions = new Region[args.length / 2];
        for (int i = 0; i < regions.length; i++) {
            regions[i] = new CommandRegion(args[2 * i], args[2 * i + 1]);
        }
        return regions;
    }

    /**
     * Checks whether the consensus generator still works when you have thwo chromosones in the vcf.
     */
    @Test
    public void testTwoChromosomes() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VCFIterator doubleIterator;
        ConsensusGenerator doubleGenerator;
        try {
            doubleIterator = new VCFIteratorBuilder().open(new File(FOLDER, "double.vcf"));
            doubleGenerator = new ConsensusGenerator(
                    new FastaSequence(fasta),
                    "double.vcf",
                    doubleIterator,
                    new IupacEncoder(),
                    new SimpleSampler(),
                    "sampleGen|");
            doubleGenerator.write(outputStream, new CommandRegion(1, 70),
                    new CompoundFilter(true), System.err);
            assertThat(new String(outputStream.toByteArray(), "UTF-8"))
                    .isEqualToNormalizingNewlines(">sampleGen|1-70|\n"
                            + "TTAGCCCCAGACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTCGACTCCGAACTTAACGGCGACC");
        } catch (IOException e) {
            fail("IOException", e);
        }
    }

    /**
     * Checks whether the consensus generator still works when you have thwo chromosones in the vcf.
     */
    @Test
    public void testTwoChromosomes2() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VCFIterator doubleIterator;
        ConsensusGenerator doubleGenerator;
        try {
            doubleIterator = new VCFIteratorBuilder().open(new File(FOLDER, "double.vcf"));
            doubleGenerator = new ConsensusGenerator(
                    new FastaSequence(fasta),
                    "double.vcf",
                    doubleIterator,
                    new IupacEncoder(),
                    new SimpleSampler(),
                    "sampleGen2|");
            doubleGenerator.write(outputStream, new CommandRegion(1, 70),
                    new CompoundFilter(true), System.err);
            assertThat(new String(outputStream.toByteArray(), "UTF-8"))
                    .isEqualToNormalizingNewlines(">sampleGen2|1-70|\n"
                            + "TTGACCGATGACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTCGTCTCCGAACTTAACGGCGACC");
        } catch (IOException e) {
            fail("IOException", e);
        }
    }

    /**
     * Checks whether the consensus generator still works when you have thwo chromosones in the vcf.
     * But don't specify which chromsone to use.
     */
    @Test
    public void testTwoChromsomes3() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VCFIterator doubleIterator;
        ConsensusGenerator doubleGenerator;
        try {
            doubleIterator = new VCFIteratorBuilder().open(new File(FOLDER, "double.vcf"));
            doubleGenerator = new ConsensusGenerator(
                    new FastaSequence(fasta),
                    "double.vcf",
                    doubleIterator,
                    new IupacEncoder(),
                    new SimpleSampler());
            doubleGenerator.write(outputStream, new CommandRegion(1, 70),
                    new CompoundFilter(true), System.err);
            assertThat(new String(outputStream.toByteArray(), "UTF-8"))
                    .isEqualToNormalizingNewlines(">sampleGen|1-70|\n"
                            + "TTAGCCCCAGACCCCGGTTCAGGCTTCACCACAGTGTGGAACGCGGTCGACTCCGAACTTAACGGCGACC");
        } catch (IOException e) {
            fail("IOException", e);
        }
    }

    /**
     * Checks whether the consensus generator still works when you have thwo chromosones in the vcf.
     * But withouth a fasta.
     */
    @Test
    public void testTwoChrosomes4() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        VCFIterator doubleIterator;
        ConsensusGenerator doubleGenerator;
        try {
            doubleIterator = new VCFIteratorBuilder().open(new File(FOLDER, "double.vcf"));
            doubleGenerator = new ConsensusGenerator(
                    null,
                    "double.vcf",
                    doubleIterator,
                    new IupacEncoder(),
                    new SimpleSampler());
            doubleGenerator.write(outputStream, new CommandRegion(1, 70),
                    new CompoundFilter(true), System.err);
            assertThat(new String(outputStream.toByteArray(), "UTF-8"))
                    .isEqualToNormalizingNewlines(">sampleGen2|1-70|\n"
                            + "..GA..GAT........................................T....................");
        } catch (IOException e) {
            fail("IOException", e);
        }
    }
}