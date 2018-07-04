package consensus.filters;

import cli.Region;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This filter filters position out that are not in a specified region.
 */
public class PositionFilter implements VariantContextFilter {
    private final Set<Region> positions;
    private final boolean overlap;

    /**
     * @inheritDocs
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PositionFilter that = (PositionFilter) o;

        if (overlap != that.overlap) {
            return false;
        }

        return positions != null ? positions.equals(that.positions) : that.positions == null;
    }

    /**
     * @inheritDocs
     */
    @Override
    public int hashCode() {
        int result = positions != null ? positions.hashCode() : 0;
        result = 31 * result + (overlap ? 1 : 0);
        return result;
    }

    /**
     * The comparator class for the tree set used in the VariantContext.
     */
    private static class PositionComparator implements Comparator<Region>, Serializable {

        /**
         * @inheritDocs
         */
        @Override
        public int compare(Region p1, Region p2) {
            if (p1.getStart() - p2.getStart() == 0) {
                if (p1.getEnd() == -1) {
                    return p2.getEnd() == -1 ? 0 : 1;
                }

                return p2.getEnd() == -1 ? -1 : p1.getEnd() - p2.getEnd();
            }

            return p1.getStart() - p2.getStart();
        }
    }

    /**
     * Creates a new PositionFilter based on the lists of positions and whether the regions
     * are allowed to overlap.
     *
     * @param positions A list of arrays with at index 0 a start position
     *                  and at index 1 an end positions.
     * @param overlap   Set this to false if the entire variant context must be included in one of the regions.
     *                  If this is true it will check if the variant context overlaps the region.
     */
    public PositionFilter(List<Region> positions, boolean overlap) {
        this.positions = new TreeSet<>(new PositionComparator());
        this.positions.addAll(positions);
        this.overlap = overlap;
    }

    /**
     * Tests whether the given variant context is included in the current filter.
     *
     * @param record The variant context.
     * @return true if the variant lies on one of the region.
     */
    @Override
    public boolean test(VariantContext record) {
        for (Region position : positions) {
            if (testPosition(record.getStart(), record.getReference().length(), position)) {
                return true;
            }
        }
        return false;
    }

    private boolean testPosition(int variantStart, int variantLength, Region position) {
        final boolean hasEnd = position.getEnd() != -1;
        if (overlap) {
            return variantStart + (variantLength - 1) >= position.getStart()
                    && (variantStart <= position.getEnd() || !hasEnd);
        } else {
            return variantStart >= position.getStart()
                    && (variantStart + (variantLength - 1) <= position.getEnd() || !hasEnd);
        }
    }
}
