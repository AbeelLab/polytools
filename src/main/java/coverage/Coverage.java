package coverage;

import lombok.Getter;

/**
 * Coverage, which represents the coverage of a (part of) a chromosome.
 */
@Getter
public class Coverage {
    /**
     * The chromosome we are looking at.
     */
    private String chromosome;

    /**
     * Inclusive start index from where we determined coverage.
     */
    private int startPos;

    /**
     * Inclusive end index until where we determined coverage.
     */
    private int endPos;

    /**
     * Total depth, which shows the depth of all bases combined.
     */
    private long totalDepth;

    /**
     * Total covered, which is the amount of bases covered by the minimal coverage.
     */
    private int totalCovered;

    /**
     * The minimal coverage after which a base is considered covered.
     */
    private int minimalCoverage;

    /**
     * Constructor initializing fields.
     *
     * @param chr             the chromosome string we looked at.
     * @param from            the start coverage index inclusive.
     * @param to              the end coverage index inclusive.
     * @param totalDepth      the total depth of coverage over all bases.
     * @param totalCovered    the total amount of bases covered in the interval.
     * @param minimalCoverage the minimal covered after which we consider a base covered.
     */
    Coverage(String chr, int from, int to, long totalDepth, int totalCovered, int minimalCoverage) {
        this.chromosome = chr;
        this.startPos = from;
        this.endPos = to;
        this.totalDepth = totalDepth;
        this.totalCovered = totalCovered;
        this.minimalCoverage = minimalCoverage;
    }

    /**
     * Add to the total depth variable.
     * @param toAdd the amount to add.
     */
    public void addToTotalDepth(long toAdd) {
        this.totalDepth += toAdd;
    }

    /**
     * Add to the total covered variable.
     * @param toAdd the amount to add.
     */
    public void addToTotalCovered(int toAdd) {
        this.totalCovered += toAdd;
    }

    /**
     * Get the total number of bases.
     * Calculated from the difference between start and and pos. + 1
     *
     * @return the difference between start and end + 1.
     */
    public int getTotalBases() {
        //the 1 plus because both the start and end are inclusive.
        return 1 + (endPos > startPos ? (endPos - startPos) : (startPos - endPos));
    }

    /**
     * Get the covered width, aka the amount of bases
     * covered divided by the total amount of bases.
     *
     * @return the covered width.
     */
    public double getCoveredWidth() {
        return (totalCovered / (double) getTotalBases());
    }

    /**
     * Get average depth, which is the total depth,
     * divided by the amount of bases.
     *
     * @return the average coverage depth.
     */
    public double getAverageDepth() {
        return totalDepth / (double) getTotalBases();
    }

    /**
     * Nice to string method for command line showing of coverage.
     * @return ar string representing this object.
     */
    @Override
    public String toString() {
        return "Coverage CHR: "
                + getChromosome() + "\nInterval: ["
                + getStartPos() + ", " + getEndPos()
                + "]\nAverage depth: " + ((int) (getAverageDepth() * 1000.0) / 1000.0)
                + "\nCovered width: " + ((int) (getCoveredWidth() * 1000.0) / 1000.0);
    }
}
