package cli;

/**
 * Indicates a region in a genome.
 * Created by regiv on 29/05/2018.
 */
public interface Region {

    /**
     * Returns the start point of this region.
     *
     * @return the start point of this region.
     */
    int getStart();

    /**
     * Returns the end point of this region. -1 if end point is undefined.
     *
     * @return the end point of this region.
     */
    int getEnd();

    /**
     * Returns true if the region is positively stranded or unknown, false if negatively stranded.
     *
     * @return true if the region is positively stranded or unknown, false if negatively stranded.
     */
    boolean getStrandedness();

    /**
     * The type of region this is.
     *
     * @return the type of region.
     */
    String getType();

    /**
     * A colliquial name for this region.
     *
     * @return the name for this region.
     */
    String getName();

    /**
     * Set region end.
     *
     * @param i the new end.
     */
    void setEnd(int i);
}

