package cli.options;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

/**
 * Created by regiv on 23/05/2018.
 * Defines the filter options for the consensus command.
 */
public class ConsensusFilterOptions {
    /**
     * Option name.
     */
    public static final String MINIMUM_ALLELE_FREQUENCY = "min-af";

    /**
     * Option name.
     */
    public static final String MAXIMUM_ALLELE_FREQUENCY = "max-af";

    /**
     * Option name.
     */
    public static final String MINIMUM_QUALITY = "min-q";

    /**
     * Option name.
     */
    public static final String MINIMUM_ALLELE_COUNT = "min-ac";

    /**
     * Option name.
     */
    public static final String MAXIMUM_ALLELE_COUNT = "max-ac";

    /**
     * Option name.
     */
    public static final String MINIMUM_DEPTH = "min-dp";

    /**
     * Option name.
     */
    public static final String MAXIMUM_DEPTH = "max-dp";

    /**
     * Option name.
     */
    public static final String EXCLUDE_POSITION = "exclude-positions";

    /**
     * Option name.
     */
    public static final String INCLUDE_POSITION = "positions";

    /**
     * Option name.
     */
    public static final String EXCLUDE_POSITION_OVERLAP = "exclude-positions-overlap";

    /**
     * Option name.
     */
    public static final String INCLUDE_POSITION_OVERLAP = "positions-overlap";

    /**
     * Option name.
     */
    public static final String REMOVE_FILTERED = "remove-filtered";

    /**
     * Option name.
     */
    public static final String REMOVE_FILTERED_ALL = "remove-filtered-all";

    /**
     * Option name.
     */
    public static final String KEEP_FILTERED = "keep-filtered";

    /**
     * Option name.
     */
    public static final String KEEP_INDELS = "keep-only-indels";

    /**
     * Option name.
     */
    public static final String REMOVE_INDELS = "remove-indels";

    /**
     * String used for skip failing context specification option and display in help message.
     */
    public static final String SKIP_FAILING_CONTEXT = "skip-failing-calls";

    /**
     * Defines the filter options for the consensus command.
     *
     * @param options Options object to add the filter options to.
     */
    public void defineFilterOptions(Options options) {
        Option minimumQualityOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MINIMUM_QUALITY)
                .desc("Include only sites where the QUAL field is greater or equal than this value")
                .build();
        options.addOption(minimumQualityOption);
        addCoverageFilterOptions(options);
        addAlleleFilterOptions(options);
        addPositionFilterOptions(options);
        addFilterFilterOptions(options);
        addIndelFilterOptions(options);
        addFailOption(options);
    }

    private void addFailOption(Options options) {
        Option skipFailingContextOption = Option.builder("sf")
                .required(false)
                .longOpt(SKIP_FAILING_CONTEXT)
                .desc("Skip calls if filters fail, if no fasta is present this will put a '.' on the index"
                        + " where the call did not pass the filter(s).")
                .build();
        options.addOption(skipFailingContextOption);
    }

    private void addCoverageFilterOptions(Options options) {
        Option minimumDepthOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MINIMUM_DEPTH)
                .desc("Include only sites with a depth greater than or equal to this value")
                .build();
        Option maximumDepthOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MAXIMUM_DEPTH)
                .desc("Include only sites with a depth less than or equal to this value")
                .build();
        options.addOption(minimumDepthOption)
                .addOption(maximumDepthOption);
    }

    private void addAlleleFilterOptions(Options options) {
        Option minimumAlleleFrequencyOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MINIMUM_ALLELE_FREQUENCY)
                .desc("Include only variants with an allele frequency greater than or equal to this value")
                .build();
        Option maximumAlleleFrequencyOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MAXIMUM_ALLELE_FREQUENCY)
                .desc("Include only variants with an allele frequency less than or equal to this value")
                .build();
        Option minimumAlleleCountOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MINIMUM_ALLELE_COUNT)
                .desc("Include only variants with an allele count greater than or equal to this value")
                .build();
        Option maximumAlleleCountOption = Option.builder()
                .required(false)
                .hasArg()
                .longOpt(MAXIMUM_ALLELE_COUNT)
                .desc("Include only variants with an allele count less than or equal to this value")
                .build();
        options.addOption(minimumAlleleCountOption)
                .addOption(minimumAlleleFrequencyOption)
                .addOption(maximumAlleleCountOption)
                .addOption(maximumAlleleFrequencyOption);
    }

    private void addIndelFilterOptions(Options options) {
        Option indelKeep = Option.builder()
                .longOpt(KEEP_INDELS)
                .required(false).hasArg(false)
                .desc("This filter will only consider indels when generating the consensus sequence").build();

        Option indelRemove = Option.builder()
                .longOpt(REMOVE_INDELS)
                .required(false).hasArg(false)
                .desc("This filter will ignore any indels when generating the consensus sequence").build();

        OptionGroup group = new OptionGroup().addOption(indelKeep).addOption(indelRemove);
        group.setRequired(false);
        options.addOptionGroup(group);
    }


    private void addPositionFilterOptions(Options options) {
        Option positionOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(INCLUDE_POSITION)
                .desc("Include only variants on these positions")
                .build();
        Option excludePositionOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(EXCLUDE_POSITION)
                .desc("Exclude all variants on these positions")
                .build();
        Option positionOverlapOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(INCLUDE_POSITION_OVERLAP)
                .desc("Include only variants overlapping with these positions")
                .build();
        Option excludePositionOverlapOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(EXCLUDE_POSITION_OVERLAP)
                .desc("Exclude all variants overlapping with these positions")
                .build();

        options.addOption(excludePositionOption)
                .addOption(excludePositionOverlapOption)
                .addOption(positionOption)
                .addOption(positionOverlapOption);
    }

    private void addFilterFilterOptions(Options options) {
        Option removeFilteredAllOption = Option.builder()
                .required(false)
                .longOpt(REMOVE_FILTERED_ALL)
                .desc("Exclude all variants with a different filter flag than PASS"
                        + "Mutually exclusive with keep-filtered and remove-filtered-all")
                .build();
        Option keepFilteredOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(KEEP_FILTERED)
                .desc("Include only variants matching a specific filter flag. "
                        + "Using this command multiple times keeps all variants matching any of these flags."
                        + "Mutually exclusive with keep-filtered and remove-filtered-all")
                .build();
        Option removeFilteredOption = Option.builder()
                .required(false)
                .hasArgs()
                .longOpt(REMOVE_FILTERED)
                .desc("Exclude all variants matching a specific filter flag."
                        + "Using this command multiple times removes all variants matching any of these flags."
                        + "Mutually exclusive with keep-filtered and remove-filtered-all")
                .build();

        OptionGroup group = new OptionGroup().addOption(removeFilteredOption)
                .addOption(keepFilteredOption)
                .addOption(removeFilteredAllOption);

        options.addOptionGroup(group);
    }

}
