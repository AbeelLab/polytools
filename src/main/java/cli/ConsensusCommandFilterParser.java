package cli;

import cli.options.ConsensusFilterOptions;
import consensus.filters.*;
import htsjdk.variant.variantcontext.filter.CompoundFilter;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by regiv on 23/05/2018.
 * Parses the user input to create filters.
 */
public class ConsensusCommandFilterParser {
    /**
     * Parses the user input to create filters.
     *
     * @param cmd The user input.
     * @return The filters as given by the user.
     * @throws IllegalArgumentException If the input is malformed.
     */
    public CompoundFilter createFilters(Command cmd) {
        CompoundFilter filters = new CompoundFilter(true);
        if (cmd.hasOption(ConsensusFilterOptions.MINIMUM_QUALITY)) {
            addMinimumQualityFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.MAXIMUM_ALLELE_COUNT)
                || cmd.hasOption(ConsensusFilterOptions.MINIMUM_ALLELE_COUNT)) {
            addAlleleCountFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.MAXIMUM_ALLELE_FREQUENCY)
                || cmd.hasOption(ConsensusFilterOptions.MINIMUM_ALLELE_FREQUENCY)) {
            addAlleleFrequencyFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.MINIMUM_DEPTH)
                || cmd.hasOption(ConsensusFilterOptions.MAXIMUM_DEPTH)) {
            addDepthFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.KEEP_FILTERED)
                || cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED)
                || cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED_ALL)) {
            addFilterFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.EXCLUDE_POSITION)) {
            addExcludePositionFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.EXCLUDE_POSITION_OVERLAP)) {
            addExcludePositionOverlapFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.INCLUDE_POSITION)) {
            addIncludePositionFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.INCLUDE_POSITION_OVERLAP)) {
            addIncludePositionOverlapFilter(cmd, filters);
        }

        if (cmd.hasOption(ConsensusFilterOptions.KEEP_INDELS)) {
            filters.add(new FilterInverse(new IndelFilter()));
        }

        if (cmd.hasOption(ConsensusFilterOptions.REMOVE_INDELS)) {
            filters.add(new IndelFilter());
        }

        return filters;
    }

    private void addAlleleCountFilter(Command cmd, List<VariantContextFilter> filters) {
        int maxAC = Integer.MAX_VALUE;
        int minAC = 0;
        if (cmd.hasOption(ConsensusFilterOptions.MAXIMUM_ALLELE_COUNT)) {
            try {
                maxAC = Integer.parseInt(cmd.getOptionValue(ConsensusFilterOptions.MAXIMUM_ALLELE_COUNT));
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("missing --max-ac value.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--max-ac needs an integer as argument.");
            }
        }

        if (cmd.hasOption(ConsensusFilterOptions.MINIMUM_ALLELE_COUNT)) {
            try {
                minAC = Integer.parseInt(cmd.getOptionValue(ConsensusFilterOptions.MINIMUM_ALLELE_COUNT));
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("missing --min-ac value.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--min-ac needs an integer as argument.");
            }
        }

        if (minAC > maxAC) {
            throw new IllegalArgumentException("minimum allele count cannot be larger than maximum allele count.");
        }

        filters.add(new AlleleCountFilter(minAC, maxAC, false));
    }

    private void addAlleleFrequencyFilter(Command cmd, List<VariantContextFilter> filters) {
        double maxAF = 1;
        double minAF = 0;
        if (cmd.hasOption(ConsensusFilterOptions.MAXIMUM_ALLELE_FREQUENCY)) {
            try {
                maxAF = Double.parseDouble(cmd.getOptionValue(ConsensusFilterOptions.MAXIMUM_ALLELE_FREQUENCY));
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("missing --max-af value.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--max-af needs an number as argument.");
            }
        }

        if (cmd.hasOption(ConsensusFilterOptions.MINIMUM_ALLELE_FREQUENCY)) {
            try {
                minAF = Double.parseDouble(cmd.getOptionValue(ConsensusFilterOptions.MINIMUM_ALLELE_FREQUENCY));
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("missing --min-af value.");
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--min-af needs an number as argument.");
            }
        }

        if (minAF > maxAF) {
            throw new IllegalArgumentException(
                    "minimum allele frequency cannot be larger than maximum allele frequency.");
        }

        filters.add(new AlleleFrequencyFilter(minAF, maxAF, false));
    }

    private void addDepthFilter(Command cmd, List<VariantContextFilter> filters) {
        int maxDepth = Integer.MAX_VALUE;
        int minDepth = 0;
        if (cmd.hasOption(ConsensusFilterOptions.MAXIMUM_DEPTH)) {
            try {
                maxDepth = Integer.parseInt(cmd.getOptionValue(ConsensusFilterOptions.MAXIMUM_DEPTH));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--max-dp needs an integer as argument.");
            }
        }

        if (cmd.hasOption(ConsensusFilterOptions.MINIMUM_DEPTH)) {
            try {
                minDepth = Integer.parseInt(cmd.getOptionValue(ConsensusFilterOptions.MINIMUM_DEPTH));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--min-dp needs an integer as argument.");
            }
        }

        if (minDepth > maxDepth) {
            throw new IllegalArgumentException("minimum depth cannot be larger than maximum depth.");
        }

        filters.add(new DepthFilter(minDepth, maxDepth));
    }

    private void addFilterFilter(Command cmd, List<VariantContextFilter> filters) {
        if (cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED)) {
            if (cmd.hasOption(ConsensusFilterOptions.KEEP_FILTERED)
                    || cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED_ALL)) {

                throw new IllegalArgumentException(
                        "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.");
            }

            if (cmd.getOptionValues(ConsensusFilterOptions.REMOVE_FILTERED) == null) {
                throw new IllegalArgumentException("No filter flags given with remove-filtered.");
            }
            String[] values = cmd.getOptionValues(ConsensusFilterOptions.REMOVE_FILTERED);
            List<String> strings = Arrays.asList(values);
            filters.add(new FilterInverse(new FilterInclude(strings, false)));
            return;
        }

        if (cmd.hasOption(ConsensusFilterOptions.KEEP_FILTERED)) {
            if (cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED_ALL)) {
                throw new IllegalArgumentException(
                        "remove-filtered, remove-filtered-all and keep-filtered are mutually exclusive.");
            }

            if (cmd.getOptionValues(ConsensusFilterOptions.KEEP_FILTERED) == null) {
                throw new IllegalArgumentException("No filter flags given with keep-filtered.");
            }

            List<String> strings = Arrays.asList(cmd.getOptionValues(ConsensusFilterOptions.KEEP_FILTERED));
            filters.add(new FilterInclude(strings, false));
            return;
        }

        if (cmd.hasOption(ConsensusFilterOptions.REMOVE_FILTERED_ALL)) {
            filters.add(new FilterInverse(new FilterInclude(Collections.singletonList("PASS"), false)));
        }
    }

    private void addExcludePositionFilter(Command cmd, ArrayList<VariantContextFilter> filters) {
        List<Region> regions = getRegions(cmd, ConsensusFilterOptions.EXCLUDE_POSITION);
        filters.add(new FilterInverse(new PositionFilter(regions, false)));
    }

    private void addExcludePositionOverlapFilter(Command cmd, ArrayList<VariantContextFilter> filters) {
        List<Region> regions = getRegions(cmd, ConsensusFilterOptions.EXCLUDE_POSITION_OVERLAP);
        filters.add(new FilterInverse(new PositionFilter(regions, true)));
    }

    private void addIncludePositionFilter(Command cmd, ArrayList<VariantContextFilter> filters) {
        List<Region> regions = getRegions(cmd, ConsensusFilterOptions.INCLUDE_POSITION);
        filters.add(new PositionFilter(regions, false));
    }

    private void addIncludePositionOverlapFilter(Command cmd, ArrayList<VariantContextFilter> filters) {
        List<Region> regions = getRegions(cmd, ConsensusFilterOptions.INCLUDE_POSITION_OVERLAP);
        filters.add(new PositionFilter(regions, true));
    }

    private void addMinimumQualityFilter(Command cmd, ArrayList<VariantContextFilter> filters) {
        try {
            String value = cmd.getOptionValue(ConsensusFilterOptions.MINIMUM_QUALITY);
            double quality = Double.parseDouble(value);
            filters.add(new FilterQuality(quality));
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("missing --min-q value.");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("--min-q needs a number as argument.");
        }
    }

    private List<Region> getRegions(Command cmd, String commandString) {
        RegionParser parser = new RegionParser();
        List<Region> regions = null;
        try {
            regions = parser.parseRegions(cmd.getOptionValues(commandString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not make filter: " + e.getMessage());
        }
        return regions;
    }
}
