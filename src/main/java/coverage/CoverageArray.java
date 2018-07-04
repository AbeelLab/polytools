package coverage;

/**
 * Coverage including an array which has the coverage per base.
 */
public class CoverageArray extends Coverage {
    /**
     * Array containing the coverage depth per base index.
     * Where index 0 is the from position, and the last index is the end position.
     */
    private int[] coveragePerBase;

    /**
     * Constructor initializing fields.
     *
     * @param chr             the chromosome string we looked at.
     * @param from            the start coverage index inclusive.
     * @param to              the end coverage index inclusive.
     * @param totalDepth      the total depth of coverage over all bases.
     * @param totalCovered    the total amount of bases covered in the interval.
     * @param minimalCoverage the minimal covered after which we consider a base covered.
     * @param coveragePerBase the coverage per base, represented as array.
     */
    CoverageArray(String chr, int from, int to, long totalDepth,
                  int totalCovered, int minimalCoverage, int[] coveragePerBase) {
        super(chr, from, to, totalDepth, totalCovered, minimalCoverage);
        this.coveragePerBase = coveragePerBase;
    }

    /**
     * Return the coverage at a given place in the array, 0 being the startIndex,
     * and the getTotalBases() - 1 being the last, or end index.
     *
     * @param i the index to get.
     * @return the depth at that base.
     */
    public int coverageAtBase(int i) {
        return coveragePerBase[i];
    }

    /**
     * Get the lowest coverage in this segment.
     *
     * @return the lowest found coverage in the array.
     */
    public int lowestCoverage() {
        int lowest = Integer.MAX_VALUE;
        for (int i : coveragePerBase) {
            if (i < lowest) {
                lowest = i;
            }
        }
        return lowest;
    }

    /**
     * Get the highest coverage in this segment.
     *
     * @return the highest found coverage in the array.
     */
    public int highestCoverage() {
        int highest = Integer.MIN_VALUE;
        for (int i : coveragePerBase) {
            if (i > highest) {
                highest = i;
            }
        }
        return highest;
    }

    /**
     * Get the amount of bases with a given coverage or higher.
     *
     * @param minimal the minimal coverage of a base to be counted.
     * @return the amount of bases with that given minimal coverage.
     */
    public int totalBasesWithMinimalCoverage(int minimal) {
        int total = 0;
        for (int i : coveragePerBase) {
            if (i >= minimal) {
                total++;
            }
        }
        return total;
    }

    /**
     * Return a nice looking coverage report for this chromosome interval.
     *
     * @param intervals the amount of intervals to show.
     * @return a nice report of the coverage.
     */
    public String coverageStatisticsOverview(int intervals) {
        StringBuilder builder = new StringBuilder();
        int low = lowestCoverage();
        int high = highestCoverage();
        builder.append("Coverage CHR: ")
                .append(getChromosome()).append("\nInterval: [")
                .append(getStartPos()).append(", ")
                .append(getEndPos()).append("]  ")
                .append("Low/High: ").append(low)
                .append("/").append(high).append("\n")
                .append("Average depth: ")
                .append((int) (getAverageDepth() * 1000.0) / 1000.0)
                .append(" - Covered width: ")
                .append((int) (getCoveredWidth() * 1000.0) / 1000.0)
                .append("\n");
        for (int i = 0; i < intervals; i++) {
            int mini = low + (int) (high * ((i + 1) / (double) intervals)) - 5;
            if (mini < 0) {
                continue;
            }
            int amount = totalBasesWithMinimalCoverage(mini);
            double percentCoverage = ((int) (((amount / (double)
                    getTotalBases()) * 100) * 100.0)) / 100.0;
            builder.append("At least ").append(mini)
                    .append(" coverage: ").append(percentCoverage)
                    .append("% (").append(amount)
                    .append(" bases)\n");
        }
        return builder.toString();
    }
}