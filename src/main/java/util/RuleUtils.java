package util;

import org.geotools.brewer.styling.filter.AndBuilder;
import org.geotools.styling.Rule;
import org.geotools.styling.RuleImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.IncludeFilter;

public class RuleUtils {

    /**
     * Checks the current filter and if it's not an IncludeFilter the current rule filter is replaced
     * by a AndFilter which contains the existing filter and the passed filter argument
     *
     * @param rule   that should be modified
     * @param filter that will be added
     */
    public static Rule combineWithExistingFilter(Rule rule, Filter filter) {
        if (rule.getFilter() != null && rule.getFilter().equals(filter))
            return rule;
        // IncludeFilter is standard filter and can be overridden
        Rule newRule = new RuleImpl(rule);
        if (rule.getFilter() == null || rule.getFilter() instanceof IncludeFilter) {
            newRule.setFilter(filter);
        } else {
            newRule.setFilter(new AndBuilder<Rule>().and(rule.getFilter()).and(filter).build());
        }
        return newRule;
    }
}
