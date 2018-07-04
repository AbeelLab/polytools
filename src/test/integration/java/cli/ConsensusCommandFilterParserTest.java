package cli;

import consensus.filters.*;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by regiv on 23/05/2018.
 */
public class ConsensusCommandFilterParserTest {

    private Command cmd;
    private ConsensusCommandFilterParser parser;

    /**
     * Set up for the tests.
     */
    @Before
    public void setUp() {
        cmd = mock(Command.class);
        parser = new ConsensusCommandFilterParser();
        when(cmd.getOptionValue(any())).thenReturn("1");
        when(cmd.getOptionValues(any())).thenReturn(new String[]{"1", "2"});
        when(cmd.hasOption(any())).thenReturn(false);
    }


    /**
     * Sanity check.
     */
    @Test
    public void testAllFilters() {
        when(cmd.hasOption(any())).thenReturn(true);
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(false);
        List<VariantContextFilter> filter = parser.createFilters(cmd);
        assertEquals(11, filter.size());
    }

    /**
     * Happy path min-q filter.
     */
    @Test
    public void testMinQ() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-q"))).thenReturn(true);
        when(cmd.getOptionValue("min-q")).thenReturn("3");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        FilterQuality expected = new FilterQuality(3);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * number format wrong min-q filter test.
     */
    @Test
    public void testMinQNotANumber() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-q"))).thenReturn(true);
        when(cmd.getOptionValue("min-q")).thenReturn("Foo");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--min-q needs a number as argument.");
        }
    }

    /**
     * no argument min-q filter test.
     */
    @Test
    public void testMinQEmpty() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-q"))).thenReturn(true);
        when(cmd.getOptionValue("min-q")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "missing --min-q value.");
        }
    }

    /**
     * wrong number format min-dp filter test.
     */
    @Test
    public void testMinDepthFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-dp"))).thenReturn(true);
        when(cmd.getOptionValue("min-dp")).thenReturn("Bar");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--min-dp needs an integer as argument.");
        }
    }

    /**
     * wrong number format max-dp filter test.
     */
    @Test
    public void testMaxDepthFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-dp"))).thenReturn(true);
        when(cmd.getOptionValue("max-dp")).thenReturn("0.5");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--max-dp needs an integer as argument.");
        }
    }

    /**
     * no argument min-max depth flipped filter test.
     */
    @Test
    public void testMinMaxDepthFilterFlipped() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-dp"))).thenReturn(true);
        when(cmd.getOptionValue("max-dp")).thenReturn("1");
        when(cmd.hasOption(eq("min-dp"))).thenReturn(true);
        when(cmd.getOptionValue("min-dp")).thenReturn("4");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "minimum depth cannot be larger than maximum depth.");
        }
    }

    /**
     * Happy path Minimum Allele Count filter.
     */
    @Test
    public void testMinAlleleCount() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-ac"))).thenReturn(true);
        when(cmd.getOptionValue("min-ac")).thenReturn("2");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        AlleleCountFilter expected = new AlleleCountFilter(2, Integer.MAX_VALUE, false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * no argument max ac filter test.
     */
    @Test
    public void testMaxAlleleCountFilterMissing() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-ac"))).thenReturn(true);
        when(cmd.getOptionValue("max-ac")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("--max-ac needs an integer as argument.", e.getMessage());
        }
    }

    /**
     * bad format max ac filter test.
     */
    @Test
    public void testMaxAlleleCountFilterFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-ac"))).thenReturn(true);
        when(cmd.getOptionValue("max-ac")).thenReturn("foo");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("--max-ac needs an integer as argument.", e.getMessage());
        }
    }

    /**
     * bad format min ac filter test.
     */
    @Test
    public void testMinAlleleCountFilterFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-ac"))).thenReturn(true);
        when(cmd.getOptionValue("min-ac")).thenReturn("1.2");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--min-ac needs an integer as argument.");
        }
    }

    /**
     * no argument min ac filter test.
     */
    @Test
    public void testMinAlleleCountFilterMissing() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-ac"))).thenReturn(true);
        when(cmd.getOptionValue("min-ac")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--min-ac needs an integer as argument.");
        }
    }

    /**
     * Happy path Maximum Allele Count filter.
     */
    @Test
    public void testMaxAlleleCount() {
        when(cmd.hasOption(eq("max-ac"))).thenReturn(true);
        when(cmd.getOptionValue("max-ac")).thenReturn("6");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        AlleleCountFilter expected = new AlleleCountFilter(0, 6, false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * no argument min-max depth flipped filter test.
     */
    @Test
    public void testMinMaxAlleleCounterFilterFlipped() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-ac"))).thenReturn(true);
        when(cmd.getOptionValue("max-ac")).thenReturn("7");
        when(cmd.hasOption(eq("min-ac"))).thenReturn(true);
        when(cmd.getOptionValue("min-ac")).thenReturn("8");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("minimum allele count cannot be larger than maximum allele count.", e.getMessage());
        }
    }

    /**
     * Happy path Min Allele Frequency filter.
     */
    @Test
    public void testMinAlleleFrequency() {
        when(cmd.hasOption(eq("min-af"))).thenReturn(true);
        when(cmd.getOptionValue("min-af")).thenReturn("0.5");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        AlleleFrequencyFilter expected = new AlleleFrequencyFilter(0.5, 1, false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path Maximum Allele Frequency fi-lter.
     */
    @Test
    public void testMaxAlleleFrequency() {
        when(cmd.hasOption(eq("max-af"))).thenReturn(true);
        when(cmd.getOptionValue("max-af")).thenReturn("0.75");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        AlleleFrequencyFilter expected = new AlleleFrequencyFilter(0, 0.75, false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * bad format max af filter test.
     */
    @Test
    public void testMaxAlleleFrequencyFilterFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-af"))).thenReturn(true);
        when(cmd.getOptionValue("max-af")).thenReturn("foo");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("--max-af needs an number as argument.", e.getMessage());
        }
    }

    /**
     * bad format min af filter test.
     */
    @Test
    public void testMinAlleleFrequencyFilterFormat() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-af"))).thenReturn(true);
        when(cmd.getOptionValue("min-af")).thenReturn("bar");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "--min-af needs an number as argument.");
        }
    }

    /**
     * no argument min af filter test.
     */
    @Test
    public void testMinAlleleFrequencyFilterMissing() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("min-af"))).thenReturn(true);
        when(cmd.getOptionValue("min-af")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "missing --min-af value.");
        }
    }


    /**
     * no argument max af filter test.
     */
    @Test
    public void testMaxAlleleFrequencyFilterMissing() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-af"))).thenReturn(true);
        when(cmd.getOptionValue("max-af")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "missing --max-af value.");
        }
    }

    /**
     * no argument min-max allele frequency flipped filter test.
     */
    @Test
    public void testMinMaxAlleleFrequencyFilterFlipped() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("max-af"))).thenReturn(true);
        when(cmd.getOptionValue("max-af")).thenReturn("7");
        when(cmd.hasOption(eq("min-af"))).thenReturn(true);
        when(cmd.getOptionValue("min-af")).thenReturn("8");
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("minimum allele frequency cannot be larger "
                    + "than maximum allele frequency.", e.getMessage());
        }
    }

    /**
     * Happy path Minimum Depth filter.
     */
    @Test
    public void testMinDepth() {
        when(cmd.hasOption(eq("min-dp"))).thenReturn(true);
        when(cmd.getOptionValue("min-dp")).thenReturn("5");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        DepthFilter expected = new DepthFilter(5, Integer.MAX_VALUE);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path Maximum Depth filter.
     */
    @Test
    public void testMaxDepth() {
        when(cmd.hasOption(eq("max-dp"))).thenReturn(true);
        when(cmd.getOptionValue("max-dp")).thenReturn("15");
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        DepthFilter expected = new DepthFilter(0, 15);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path position filter.
     */
    @Test
    public void testPositions() {
        when(cmd.hasOption(eq("positions"))).thenReturn(true);
        when(cmd.getOptionValues("positions")).thenReturn(new String[]{"1-2", "3-5"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        PositionFilter expected = new PositionFilter(
                Arrays.asList(new CommandRegion(1, 2), new CommandRegion(3, 5)), false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path overlapping position filter test.
     */
    @Test
    public void testPositionsOverlap() {
        when(cmd.hasOption(eq("positions-overlap"))).thenReturn(true);
        when(cmd.getOptionValues("positions-overlap")).thenReturn(new String[]{"1-2", "3-5"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        PositionFilter expected = new PositionFilter(
                Arrays.asList(new CommandRegion(1, 2), new CommandRegion(3, 5)), true);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path exclude position filter test.
     */
    @Test
    public void testExcludePositions() {
        when(cmd.hasOption(eq("exclude-positions"))).thenReturn(true);
        when(cmd.getOptionValues("exclude-positions")).thenReturn(new String[]{"1-2", "3-5"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        VariantContextFilter expected = new FilterInverse(new PositionFilter(
                Arrays.asList(new CommandRegion(1, 2), new CommandRegion(3, 5)), false));
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path overlapping exclude position filter test.
     */
    @Test
    public void testExcludePositionsOverlap() {
        when(cmd.hasOption(eq("exclude-positions-overlap"))).thenReturn(true);
        when(cmd.getOptionValues("exclude-positions-overlap")).thenReturn(new String[]{"1-2", "3-5"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        VariantContextFilter expected = new FilterInverse(new PositionFilter(
                Arrays.asList(new CommandRegion(1, 2), new CommandRegion(3, 5)), true));
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Happy path remove filtered test.
     */
    @Test
    public void testRemoveFiltered() {
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(true);
        when(cmd.getOptionValues("remove-filtered")).thenReturn(new String[]{"PASS", "Amb"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        VariantContextFilter expected = new FilterInverse(new FilterInclude(
                Arrays.asList("PASS", "Amb"), false));
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }


    /**
     * Happy path remove filtered test.
     */
    @Test
    public void testKeepFiltered() {
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(true);
        when(cmd.getOptionValues("keep-filtered")).thenReturn(new String[]{"LowCov", "Amb"});
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        VariantContextFilter expected = new FilterInclude(
                Arrays.asList("LowCov", "Amb"), false);
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * No argument remove filtered test.
     */
    @Test
    public void testRemoveFilteredNoArgument() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(true);
        when(cmd.getOptionValues("remove-filtered")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "No filter flags given with remove-filtered.",
                    e.getMessage());
        }
    }

    /**
     * No argument remove filtered test.
     */
    @Test
    public void testKeepFilteredNoArgument() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(true);
        when(cmd.getOptionValues("keep-filtered")).thenReturn(null);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "No filter flags given with keep-filtered.",
                    e.getMessage());
        }
    }

    /**
     * Happy path remove filtered all test.
     */
    @Test
    public void testRemoveFilteredAll() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered-all"))).thenReturn(true);
        List<VariantContextFilter> filters = parser.createFilters(cmd);
        VariantContextFilter expected = new FilterInverse(new FilterInclude(
                Arrays.asList("PASS"), false));
        assertEquals(expected, filters.get(0));
        assertEquals(1, filters.size());
    }

    /**
     * Mutually exclusive remove filtered & keep filtered test.
     */
    @Test
    public void testExclusiveRemoveKeepFiltered() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(true);
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(true);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.",
                    e.getMessage());
        }
    }

    /**
     * Mutually exclusive remove all filtered & keep filtered test.
     */
    @Test
    public void testExclusiveRemoveAllKeepFiltered() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered-all"))).thenReturn(true);
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(true);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.",
                    e.getMessage());
        }
    }

    /**
     * Mutually exclusive remove filtered & remove all filtered test.
     */
    @Test
    public void testExclusiveRemoveRemoveAllFiltered() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(true);
        when(cmd.hasOption(eq("remove-filtered-all"))).thenReturn(true);
        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.",
                    e.getMessage());
        }
    }

    /**
     * Mutually exclusive remove all, remove filtered & keep filtered test.
     */
    @Test
    public void testExclusiveAllFiltered() {
        when(cmd.hasOption(any())).thenReturn(false);
        when(cmd.hasOption(eq("remove-filtered"))).thenReturn(true);
        when(cmd.hasOption(eq("remove-filtered-all"))).thenReturn(true);
        when(cmd.hasOption(eq("keep-filtered"))).thenReturn(true);

        try {
            List<VariantContextFilter> filters = parser.createFilters(cmd);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.",
                    e.getMessage());
        }
    }
}