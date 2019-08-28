package util;

import org.geotools.brewer.styling.builder.RuleBuilder;
import org.geotools.styling.Rule;
import org.junit.jupiter.api.Test;
import org.opengis.filter.Filter;

import static org.junit.jupiter.api.Assertions.*;

class RuleUtilsTest {

    @Test
    void testCombineWithExistingFilter() {
        RuleBuilder ruleBuilder = new RuleBuilder();
        ruleBuilder.filter("subclassname = 'undefined'");
        Rule firstRule = ruleBuilder.build();

        assertNotNull(firstRule);

        ruleBuilder = new RuleBuilder();
        ruleBuilder.filter("POP_RANK > 3 AND POP_RANK < 6");
        Rule secondRule = ruleBuilder.build();

        Filter firstRuleFilter = firstRule.getFilter();
        Filter secondRuleFilter = secondRule.getFilter();

        Rule newRule = RuleUtils.combineWithExistingFilter(firstRule, secondRuleFilter);

        assertNotEquals(firstRuleFilter, newRule.getFilter());
    }

    @Test
    void testCombiningSameFilters() {
        RuleBuilder ruleBuilder = new RuleBuilder();
        ruleBuilder.filter("subclassname = 'undefined'");
        Rule firstRule = ruleBuilder.build();

        assertNotNull(firstRule);

        ruleBuilder = new RuleBuilder();
        ruleBuilder.filter("subclassname = 'undefined'");
        Rule secondRule = ruleBuilder.build();

        Filter firstRuleFilter = firstRule.getFilter();
        Filter secondRuleFilter = secondRule.getFilter();

        Rule newRule = RuleUtils.combineWithExistingFilter(firstRule, secondRuleFilter);

        assertEquals(firstRuleFilter, newRule.getFilter());
    }
}