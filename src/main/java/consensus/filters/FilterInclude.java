package consensus.filters;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A class that filters on the "filter" column in the VCF file.
 */
@EqualsAndHashCode
public class FilterInclude implements VariantContextFilter {

    private final List<String> filters;
    private final boolean matchAll;

    /**
     * Creates a new filter from the filter attribute in the vcf file.
     * Allowed filters PASS, LowCov, Amb, DEL
     *
     * @param filters  A list with all the filters strings.
     * @param matchAll if <code>true</code> a variant context must
     *                 pass all filters, otherwise it must one of the filters.
     */
    public FilterInclude(List<String> filters, boolean matchAll) {
        this.filters = filters.stream().map(String::toLowerCase).collect(Collectors.toList());
        this.matchAll = matchAll;
    }

    /**
     * Tests whether the VariantContext is included in the current filter.
     * If this filter is set to matchAll the VariantContext needs to contain all filters.
     * Otherwise the VariantContext needs to contain one of the filters.
     *
     * @param variantContext The variantContexts from an vcf file.
     * @return if included in the filter.
     */
    @Override
    public boolean test(VariantContext variantContext) {
        final Set<String> filterSet = variantContext.getFilters().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        if (this.matchAll) {
            return testMatchAll(filterSet);
        }
        return testMatchAny(filterSet);
    }

    /**
     * Tests whether the VariantContext is included in the current filter.
     *
     * @param filterSet The filters of the VariantContext.
     * @return if included in the filter.
     */
    private boolean testMatchAny(Set<String> filterSet) {
        if (filterSet.size() == 0) {
            return this.filters.contains("pass");
        }
        for (String s : this.filters) {
            if (filterSet.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the VariantContext is included in the current filter.
     *
     * @param filterSet The filters of the VariantContext
     * @return if included in the filter.
     */
    private boolean testMatchAll(Set<String> filterSet) {
        if (filterSet.size() == 0) {
            return this.filters.contains("pass") && this.filters.size() == 1;
        }
        for (String s : this.filters) {
            if (!filterSet.contains(s)) {
                return false;
            }
        }
        return true;
    }
}
