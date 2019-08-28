package generation.processing;

import model.styling.MapFeature;
import model.styling.MapFeatureSubclass;
import model.styling.PlaceholderRule;
import model.styling.StyleGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.styling.Rule;
import org.jetbrains.annotations.Nullable;
import util.RuleUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

class PlaceholderRuleResolver {

    private static final Logger logger = LogManager.getLogger(PlaceholderRuleResolver.class);

    private PlaceholderRuleResolver() {
    }

    /**
     * This method will find and replace existing PlaceholderRules with the Rules they refer to.
     * Will result in a side effect on the given StyleGroups and MapFeatures .
     *
     * @param mapFeatures MapFeatures that contain PlaceholderRules
     * @param styleGroups StyleGroups that contain PlaceholderRules and normal Rules
     * @return a new collection of MapFeatures with resolved PlaceholderRules
     */
    public static Collection<MapFeature> resolvePlaceholderRules(Collection<MapFeature> mapFeatures, Collection<StyleGroup> styleGroups) {
        Collection<MapFeature> copyOfMapFeatures = new ArrayList<>(mapFeatures);

        resolvePlaceholderRulesInStyleGroups(styleGroups);
        resolvePlaceholderRulesInMapFeatures(copyOfMapFeatures, styleGroups);

        return copyOfMapFeatures;
    }

    private static void resolvePlaceholderRulesInStyleGroups(Collection<StyleGroup> styleGroups) {
        Collection<StyleGroup> styleGroupsWithImplementedRules = getStyleGroupsWithImplementedRules(styleGroups);
        Collection<StyleGroup> styleGroupsWithPlaceholderRules = getStyleGroupsThatContainPlaceholderRules(styleGroups);

        styleGroupsWithImplementedRules.forEach(styleGroupWithImplementedRules -> {
            for (StyleGroup styleGroupWithPlaceholderRule : styleGroupsWithPlaceholderRules) {
                tryReplacingPlaceholderRuleFor(styleGroupWithPlaceholderRule.getRules(), styleGroupWithImplementedRules.getRules());
            }
        });

        // if stylegroups with remaining placeholder rules exist print error
        Collection<StyleGroup> remainingStyleGroupsThatContainPlaceholderRules = getStyleGroupsThatContainPlaceholderRules(styleGroups);
        remainingStyleGroupsThatContainPlaceholderRules.forEach(styleGroup -> {
            logger.error("StyleGroup {} is referencing other styles that contain references. " +
                    "You can only reference styles that don't contain references themselves.", styleGroup.getName());
        });
    }

    private static Collection<StyleGroup> getStyleGroupsWithImplementedRules(Collection<StyleGroup> styleGroups) {
        Collection<StyleGroup> styleGroupsWithImplementedRules = new ArrayList<>();

        styleGroups.forEach(styleGroup -> {
            List<Rule> ruleList = styleGroup.getRules().stream()
                    .filter(rule -> !(rule instanceof PlaceholderRule))
                    .collect(Collectors.toList());
            if (!ruleList.isEmpty()) {
                styleGroupsWithImplementedRules.add(styleGroup);
            }
        });

        return styleGroupsWithImplementedRules;
    }

    private static Collection<StyleGroup> getStyleGroupsThatContainPlaceholderRules(Collection<StyleGroup> styleGroups) {
        Collection<StyleGroup> styleGroupsWithPlaceholderRules = new ArrayList<>();

        styleGroups.forEach(styleGroup -> {
            List<Rule> ruleList = styleGroup.getRules().stream()
                    .filter(rule -> rule instanceof PlaceholderRule)
                    .collect(Collectors.toList());
            if (!ruleList.isEmpty()) {
                styleGroupsWithPlaceholderRules.add(styleGroup);
            }
        });

        return styleGroupsWithPlaceholderRules;
    }

    private static void tryReplacingPlaceholderRuleFor(Collection<Rule> rulesWithPlaceholders, Collection<Rule> implementedRules) {
        ArrayList<Rule> rulesWithReplacedPlaceholders = new ArrayList<>();
        Iterator<Rule> ruleIterator = rulesWithPlaceholders.iterator();
        while (ruleIterator.hasNext()) {
            Rule rule = ruleIterator.next();

            if (rule instanceof PlaceholderRule) {
                String nameOfOriginal = ((PlaceholderRule) rule).getNameOfOriginal();

                Rule implementedRuleForPlaceholder = implementedRules.stream()
                        .filter(implementedRule -> implementedRule.getName().equals(nameOfOriginal))
                        .findFirst().orElse(null);

                if (implementedRuleForPlaceholder != null) {
                    ruleIterator.remove();
                    rulesWithReplacedPlaceholders.add(implementedRuleForPlaceholder);
                }
            }
        }

        rulesWithPlaceholders.addAll(rulesWithReplacedPlaceholders);
    }

    private static void resolvePlaceholderRulesInMapFeatures(Collection<MapFeature> mapFeatures, Collection<StyleGroup> styleGroups) {
        for (MapFeature mapFeature : mapFeatures) {
            Collection<MapFeatureSubclass> subclassesThatContainPlaceholderRules = getSubclassesThatContainPlaceholderRules(mapFeature.getSubclasses());

            subclassesThatContainPlaceholderRules.forEach(mapFeatureSubclass -> {
                replacePlaceholdersInSubclass(mapFeatureSubclass, styleGroups);
            });
        }
    }

    private static Collection<MapFeatureSubclass> getSubclassesThatContainPlaceholderRules(Collection<MapFeatureSubclass> mapFeatureSubclasses) {
        Collection<MapFeatureSubclass> subclassesWithPlaceholders = new ArrayList<>();

        mapFeatureSubclasses.forEach(mapFeatureSubclass -> {
            List<Rule> ruleList = mapFeatureSubclass.getRules().stream()
                    .filter(rule -> rule instanceof PlaceholderRule)
                    .collect(Collectors.toList());
            if (!ruleList.isEmpty()) {
                subclassesWithPlaceholders.add(mapFeatureSubclass);
            }
        });

        return subclassesWithPlaceholders;
    }

    private static void replacePlaceholdersInSubclass(MapFeatureSubclass subclass, Collection<StyleGroup> styleGroups) {
        Collection<PlaceholderRule> placeholderRules = getStylePlaceholderRulesFromSubclass(subclass);

        placeholderRules.forEach(placeholderRule -> {
            String nameOfOriginal = placeholderRule.getNameOfOriginal();

            StyleGroup styleGroupWithName = findStyleGroupWithName(styleGroups, nameOfOriginal);
            if (styleGroupWithName != null) {
                Collection<Rule> rulesWithModifiedZoom = createCopyOfRulesBasedOnPlaceholder(styleGroupWithName.getRules(), placeholderRule);
                subclass.getRules().addAll(rulesWithModifiedZoom);
            } else {
                logger.error("Subclass {} is referencing non-existent StyleGroup {}", subclass.getSubclassName(), nameOfOriginal);
            }
        });

        subclass.getRules().removeAll(placeholderRules);
    }

    private static Collection<PlaceholderRule> getStylePlaceholderRulesFromSubclass(MapFeatureSubclass subclass) {
        return subclass.getRules().stream()
                .filter(rule -> rule instanceof PlaceholderRule)
                .map(rule -> (PlaceholderRule) rule)
                .collect(Collectors.toList());
    }

    @Nullable
    private static StyleGroup findStyleGroupWithName(Collection<StyleGroup> styleGroups, String name) {
        List<StyleGroup> styleGroupsWithName = styleGroups.stream()
                .filter(styleGroup -> styleGroup.getName().equals(name))
                .collect(Collectors.toList());
        if (!styleGroupsWithName.isEmpty()) {
            return styleGroupsWithName.get(0);
        } else {
            return null;
        }
    }

    private static Collection<Rule> createCopyOfRulesBasedOnPlaceholder(Collection<Rule> rulesWithoutZoom, Rule placeholderRule) {
        Collection<Rule> modifiedRules = new ArrayList<>();

        rulesWithoutZoom.forEach(rule -> {
            // combine original filters with the one of the placeholderRule
            Rule modifiedRule = RuleUtils.combineWithExistingFilter(rule, placeholderRule.getFilter());
            modifiedRule.setMinScaleDenominator(placeholderRule.getMinScaleDenominator());
            modifiedRule.setMaxScaleDenominator(placeholderRule.getMaxScaleDenominator());
            modifiedRules.add(modifiedRule);
        });

        return modifiedRules;
    }
}
