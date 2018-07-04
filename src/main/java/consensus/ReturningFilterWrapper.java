package consensus;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.filter.VariantContextFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * A filter wrapper which does test a record,
 * but instead of returning false, it always
 * returns true and modifies the record to show
 * that it did not pass the test.
 */
public class ReturningFilterWrapper implements VariantContextFilter {
    private final VariantContextFilter filter;

    /**
     * Basic constructor.
     * @param filter the original filters.
     */
    ReturningFilterWrapper(VariantContextFilter filter) {
        this.filter = filter;
    }

    /**
     * Test a record by the original filter,
     * if false, add a "FILTER_FAIL" attribute.
     * @param record the record.
     * @return always true.
     */
    @Override
    public boolean test(VariantContext record) {
        if (!filter.test(record)) {
            //due to variant context maps being unmodifiable, the only way to add an attribute is this way,
            // which is basically just an exploit in their code that I found xD
            Map<String, Object> unmodifiableMap = record.getAttributes();
            Map<String, Object> modifiableCopy = new HashMap<>(unmodifiableMap);
            modifiableCopy.put("FILTER_FAIL", null);
            record.getCommonInfo().setAttributes(modifiableCopy);
        }
        //always passes
        return true;
    }
}

