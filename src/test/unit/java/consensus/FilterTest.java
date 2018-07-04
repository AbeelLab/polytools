package consensus;

import cli.CommandRegion;
import cli.Region;
import consensus.filters.*;
import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.FilteringIterator;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import org.junit.Before;
import org.junit.Test;
import vcf.VCF;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the filter.
 */
public class FilterTest {

    /**
     * Calculates the number of items in the iterator.
     *
     * @param iterator The iterator.
     * @return The amount of elements in the iterator.
     */
    public static int countElements(FilteringIterator iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /**
     * Creates a new iterator.
     *
     * @param filter The filter
     * @return The iterator.
     */
    public FilteringIterator newIterator(VariantContextFilter filter) {
        return new FilteringIterator(vcf.query("genome", 1, Integer.MAX_VALUE), filter);
    }

    private VCF vcf;

    /**
     * Initialzes the variables.
     */
    @Before
    public void init() {
        try {
            vcf = new VCF("./src/test/resources/ConsensusGenerator/filtersample.vcf");
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    /**
     * Tests that if the references overlaps one of the regions that it is included.
     */
    @Test
    public void testRegionsOverlap() {
        List<Region> positions = new ArrayList<>();
        positions.add(new CommandRegion(1, 400));
        positions.add(new CommandRegion(24696, 24700));

        VariantContextFilter filter = new PositionFilter(positions, true);
        FilteringIterator iterator = newIterator(filter);

        assertThat(countElements(iterator)).isEqualTo(2);
    }

    /**
     * Tests that only references that are fully included in the region are included.
     */
    @Test
    public void testRegionsNoOverlap() {

        List<Region> positions = new ArrayList<>();
        positions.add(new CommandRegion(1, 400));
        positions.add(new CommandRegion(24696, 24700));

        VariantContextFilter filter = new PositionFilter(positions, false);
        FilteringIterator iterator = newIterator(filter);

        assertThat(countElements(iterator)).isEqualTo(1);
    }

    /**
     * Test the filter based on the allele count attribute.
     */
    @Test
    public void testAlleleCount() {
        VariantContextFilter filter = new AlleleCountFilter(2, 4, false);
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(19 - 4);
    }

    /**
     * Test the filter based on the allele frequency attribute.
     */
    @Test
    public void testAlleleFreq() {
        VariantContextFilter filter = new AlleleFrequencyFilter(0.3, 0.95, false);
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(2);
    }

    /**
     * Test the filter based on the chr filter attribute.
     */
    @Test
    public void testChrFilter() {
        VariantContextFilter filter = new ChromosomeFilter("henk");
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(0);
    }

    /**
     * Test the filter based on the chr filter attribute.
     */
    @Test
    public void testChrFilter1() {
        VariantContextFilter filter = new ChromosomeFilter("genome");
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(19);
    }

    /**
     * Test the filter based on the depth of the alleles.
     */
    @Test
    public void testDepth() {
        VariantContextFilter filter = new DepthFilter(2, 12);
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(8);
    }

    /**
     * Test that if no depth is present in the field it is not part of the filter.
     */
    @Test
    public void testNoDepthPresent() {
        VariantContext context = mock(VariantContext.class);
        when(context.getAttribute("DP")).thenReturn(null);
        DepthFilter depthFilter = new DepthFilter(0, Integer.MAX_VALUE);
        assertThat(depthFilter.test(context)).isFalse();
    }

    /**
     * Test that if no AC is present in the field it is not part of the filter.
     */
    @Test
    public void testNoACPresent() {
        VariantContext context = mock(VariantContext.class);
        when(context.getAttribute("AC")).thenReturn(null);
        AlleleCountFilter alleleCountFilter = new AlleleCountFilter(0, Integer.MAX_VALUE, false);
        assertThat(alleleCountFilter.test(context)).isFalse();
    }

    /**
     * Test that if no AF is present in the field it is not part of the filter.
     */
    @Test
    public void testNoAFPresent() {
        VariantContext context = mock(VariantContext.class);
        when(context.getAttribute("AF")).thenReturn(null);
        AlleleFrequencyFilter frequencyFilter = new AlleleFrequencyFilter(0, Integer.MAX_VALUE, false);
        assertThat(frequencyFilter.test(context)).isFalse();
    }

    /**
     * Test that if the AC is not in the given region that the filter is false.
     */
    @Test
    public void testAlleleCountMatchAll() {
        VariantContext context = mock(VariantContext.class);
        when(context.getAttribute("AC")).thenReturn("0");
        AlleleCountFilter alleleCountFilter = new AlleleCountFilter(10, 20, true);
        assertThat(alleleCountFilter.test(context)).isFalse();
    }

    /**
     * Test that if the AC is not in the given region that the filter is false.
     */
    @Test
    public void testAlleleFrequencyMatchAll() {
        VariantContext context = mock(VariantContext.class);
        when(context.getAttribute("AF")).thenReturn("0");
        AlleleFrequencyFilter alleleCountFilter = new AlleleFrequencyFilter(10.0, 20.0, true);
        assertThat(alleleCountFilter.test(context)).isFalse();
    }

    /**
     * Test that an empty region is considered as the full region.
     */
    @Test
    public void testPosition() {
        VariantContext context = mock(VariantContext.class);
        Allele allele = mock(Allele.class);
        when(allele.length()).thenReturn(1);
        when(context.getStart()).thenReturn(20);
        when(context.getReference()).thenReturn(allele);
        PositionFilter filter = new PositionFilter(Collections.singletonList(new CommandRegion(0, -1)), true);
        assertThat(filter.test(context)).isTrue();
    }

    /**
     * Test that mutlple empty regions are considered as the full region.
     */
    @Test
    public void testPosition2() {
        VariantContext context = mock(VariantContext.class);
        Allele allele = mock(Allele.class);
        when(allele.length()).thenReturn(1);
        when(context.getStart()).thenReturn(20);
        when(context.getReference()).thenReturn(allele);
        PositionFilter filter = new PositionFilter(
                Arrays.asList(new CommandRegion(0, -1), new CommandRegion(0, -1), new CommandRegion(1, -1)),
                true);
        assertThat(filter.test(context)).isTrue();
    }

    /**
     * Test for the includeFilter.
     */
    @Test
    public void testFilterInclude1() {
        VariantContext context = mock(VariantContext.class);
        when(context.getFilters()).thenReturn(Collections.emptySet());
        FilterInclude filter = new FilterInclude(Collections.singletonList("pass"), true);
        assertThat(filter.test(context)).isTrue();
    }

    /**
     * Test for the includeFilter.
     */
    @Test
    public void testFilterInclude2() {
        VariantContext context = mock(VariantContext.class);
        when(context.getFilters()).thenReturn(Collections.emptySet());
        FilterInclude filter = new FilterInclude(Collections.singletonList("amb"), true);
        assertThat(filter.test(context)).isFalse();
    }

    /**
     * Test for the includeFilter.
     */
    @Test
    public void testFilterInclude3() {
        VariantContext context = mock(VariantContext.class);
        when(context.getFilters()).thenReturn(Collections.emptySet());
        FilterInclude filter = new FilterInclude(Arrays.asList("pass", "amb"), true);
        assertThat(filter.test(context)).isFalse();
    }

    /**
     * Test for the includeFilter.
     */
    @Test
    public void testFilterInclude4() {
        VariantContext context = mock(VariantContext.class);
        when(context.getFilters()).thenReturn(Collections.emptySet());
        FilterInclude filter = new FilterInclude(Arrays.asList("lowcov", "amb"), true);
        assertThat(filter.test(context)).isFalse();
    }

    /**
     * Test the indels filter.
     */
    @Test
    public void testIndels() {
        VariantContextFilter filter = new IndelFilter();
        FilteringIterator iterator = newIterator(filter);
        assertThat(countElements(iterator)).isEqualTo(17);
    }
}
