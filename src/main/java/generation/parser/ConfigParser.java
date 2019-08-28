package generation.parser;

import model.styling.*;
import model.styling.zoom.ZoomRegion;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.styling.css.CssParser;
import org.geotools.styling.css.CssTranslator;
import org.geotools.styling.css.Stylesheet;
import org.opengis.filter.Filter;
import org.opengis.style.Style;
import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ValueStack;
import org.parboiled.support.Var;
import util.FileHelper;
import util.RuleUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "Convert2Lambda"})
@BuildParseTree
public class ConfigParser extends BaseParser<Object> {

    protected ConfigParser() {
    }

    private static ConfigParser INSTANCE;

    public static ConfigParser getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Parboiled.createParser(ConfigParser.class);
        }

        return INSTANCE;
    }

    public static ConfigParseResult parse(File file) throws ParseException, IOException {
        String input = FileHelper.getFileContentAsString(file);
        return parse(input);
    }

    public static ConfigParseResult parse(String input) throws ParseException {
        ConfigParser parser = getInstance();
        ParseRunner<ConfigParseResult> runner = new ReportingParseRunner<>(parser.OHDMConfig());
        ParsingResult<ConfigParseResult> result = runner.run(input);
        if (result.hasErrors()) {
            throw new ParseException(result.parseErrors);
        }

        return result.parseTreeRoot.getValue();
    }

    Rule OHDMConfig() {
        return Sequence(
                ZeroOrMore(MapFeatureDeclaration()),
                ZeroOrMore(StyleGroupDeclaration()),
                EOI,
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        List<StyleGroup> styleGroups = popAll(StyleGroup.class);
                        List<MapFeature> mapFeatures = popAll(MapFeature.class);
                        push(new ConfigParseResult(mapFeatures, styleGroups));
                        return true;
                    }

                }
        );
    }

    Rule MapFeatureDeclaration() {
        Var<String> mapFeatureName = new Var<>();
        Var<String> mapFeatureReferenceName = new Var<>();

        return Sequence(
                MapFeatureName(mapFeatureName),
                BeginBlockSymbol(),
                FirstOf(MapFeatureReferenceDeclaration(mapFeatureReferenceName),
                        OneOrMore(MapFeatureSubclassDeclaration())),
                EndBlockSymbol(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        List<MapFeatureSubclass> mapFeatureSubclasses = popAll(MapFeatureSubclass.class);
                        if (mapFeatureReferenceName.get() != null) {
                            push(new MapFeatureReference(mapFeatureName.get(), mapFeatureReferenceName.get()));
                        } else {
                            push(new MapFeature(mapFeatureName.get(), mapFeatureSubclasses));
                        }
                        return true;
                    }
                }
        );
    }

    Rule MapFeatureSubclassDeclaration() {
        return Sequence(
                SubclassName(),
                BeginBlockSymbol(),
                OneOrMore(SubclassRuleDeclaration()),
                EndBlockSymbol(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        List<org.geotools.styling.Rule> subclassRules = popAll(org.geotools.styling.Rule.class);
                        List<MapFeatureReference> mapFeatureReferences = popAll(MapFeatureReference.class);
                        String subclassName = (String) pop();

                        if (mapFeatureReferences.size() > 0) {
                            push(mapFeatureReferences.get(0));
                        } else {
                            // add filter with subclassname = 'subclassname' to every rule of this subclass
                            subclassRules.forEach((rule -> {
                                Filter filter = null;
                                try {
                                    filter = ECQL.toFilter("subclassname = '" + subclassName + "'");
                                } catch (CQLException e) {
                                    e.printStackTrace();
                                }

                                org.geotools.styling.Rule newRule = RuleUtils.combineWithExistingFilter(rule, filter);
                                rule.setFilter(newRule.getFilter());
                            }));

                            push(new MapFeatureSubclass(subclassName, subclassRules));
                        }
                        return true;
                    }
                }
        );
    }

    Rule MapFeatureReferenceDeclaration(Var<String> mapFeatureReferenceName) {
        return Sequence(
                String("sameAs"),
                OptionalWhiteSpace(),
                EqualsSymbol(),
                OptionalWhiteSpace(),
                MapFeatureReferenceName(),
                OptionalWhiteSpace(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        String referenceName = (String) pop();
                        mapFeatureReferenceName.set(referenceName);
                        return true;
                    }
                }
        );
    }

    Rule MapFeatureReferenceName() {
        return Sequence(String(), push(match()));
    }

    Rule SubclassRuleDeclaration() {
        return Sequence(
                ZoomRegionDeclaration(),
                BeginBlockSymbol(),
                StyleDeclaration(),
                EndBlockSymbol(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        Collection<org.geotools.styling.Rule> rules = popAll(org.geotools.styling.Rule.class);
                        ZoomRegion zoomRegion = pop(ZoomRegion.class);

                        rules.forEach((rule) -> {
                            rule.setMinScaleDenominator(zoomRegion.getMinScaleDenominator());
                            rule.setMaxScaleDenominator(zoomRegion.getMaxScaleDenominator());
                            push(rule);
                        });
                        return true;
                    }
                }
        );
    }

    Rule ZoomRegionDeclaration() {
        return Sequence(
                OptionalWhiteSpace(),
                BeginVariableSymbol(),
                FirstOf(
                        Sequence(ZoomNumber(), "-", ZoomNumber()),
                        Sequence(">", ZoomNumber()),
                        Sequence("<", ZoomNumber()),
                        String("default")
                ),
                push(match()),
                EndVariableSymbol(),
                OptionalWhiteSpace(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        String zoomRegionString = (String) pop();
                        if (zoomRegionString.toLowerCase().equals("default")) {
                            push(ZoomRegion.getEmptyZoomRegion());
                        } else if (ZoomRegion.isValidZoomRegion(zoomRegionString)) {
                            push(new ZoomRegion(zoomRegionString));
                        }
                        return true;
                    }
                }
        );
    }

    Rule StyleGroupDeclaration() {
        return Sequence(
                StyleGroupName(),
                BeginBlockSymbol(),
                StyleDeclaration(),
                EndBlockSymbol(),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        Collection<org.geotools.styling.Rule> rules = popAll(org.geotools.styling.Rule.class);
                        String styleGroupName = (String) pop();
                        rules.forEach(rule -> rule.setName(styleGroupName));
                        push(new StyleGroup(styleGroupName, rules));
                        return true;
                    }
                }
        );
    }

    Rule StyleDeclaration() {
        return Sequence(
                OptionalWhiteSpace(),
                FirstOf(StylePlaceholderDeclaration(), DetailedStyleDeclaration()),
                OptionalWhiteSpace()
        );
    }

    Rule StylePlaceholderDeclaration() {
        return Sequence(
                String("useStyle"),
                OptionalWhiteSpace(),
                EqualsSymbol(),
                OptionalWhiteSpace(),
                StyleNames(),
                push(match()),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        String referencingStyleGroupNames = (String) pop();
                        if (referencingStyleGroupNames.contains(",")) {
                            String[] styleGroupNames = referencingStyleGroupNames.split(",");
                            for (String styleGroupName : styleGroupNames) {
                                PlaceholderRule placeholderRule = new PlaceholderRule(styleGroupName.trim());
                                push(placeholderRule);
                            }
                        } else {
                            PlaceholderRule placeholderRule = new PlaceholderRule(referencingStyleGroupNames.trim());
                            push(placeholderRule);
                        }
                        return true;
                    }
                }
        );
    }

    Rule DetailedStyleDeclaration() {
        return Sequence(
                generation.parser.css.CssParser.getInstance().StyleSheet(),
                push(match()),
                new Action() {
                    @Override
                    public boolean run(Context context) {
                        String match = (String) pop();
                        Stylesheet stylesheet = CssParser.parse(match);
                        CssTranslator cssTranslator = new CssTranslator();
                        Style style = cssTranslator.translate(stylesheet);
                        style.featureTypeStyles().forEach((featureTypeStyle) -> featureTypeStyle.rules().forEach((rule) -> push(rule)));
                        return true;
                    }
                });
    }


    Rule MapFeatureName(Var<String> mapFeatureName) {
        return Sequence(
                BeginVariableSymbol(),
                String(),
                mapFeatureName.set(match()),
                EndVariableSymbol()
        );
    }

    Rule StyleGroupName() {
        return Sequence(
                String("<"),
                String(),
                push(match()),
                String(">")
        );
    }

    Rule SubclassName() {
        return Sequence(
                BeginVariableSymbol(),
                String(),
                push(match()),
                EndVariableSymbol()
        );
    }

    Rule StyleNames() {
        return FirstOf(
                Sequence(OneOrMore(String(), OptionalWhiteSpace(), String(","), OptionalWhiteSpace()), String()),
                String());
    }

    Rule ZoomNumber() {
        return Sequence(Digit(), Optional(Digit()));
    }

    Rule String() {
        return Sequence(AlphabeticCharacter(), ZeroOrMore(NonReservedCharacter()));
    }

    Rule NonReservedCharacter() {
        return FirstOf(AlphanumericCharacter(), AnyOf("-_"));
    }

    @SuppressNode
    Rule EqualsSymbol() {
        return String("=");
    }

    @SuppressNode
    Rule BeginVariableSymbol() {
        return String("[");
    }

    @SuppressNode
    Rule EndVariableSymbol() {
        return String("]");
    }

    @SuppressNode
    Rule BeginBlockSymbol() {
        return Sequence(OptionalWhiteSpace(), String("{"), OptionalWhiteSpace());
    }

    @SuppressNode
    Rule EndBlockSymbol() {
        return Sequence(OptionalWhiteSpace(), String("}"), OptionalWhiteSpace());
    }

    @SuppressNode
    Rule AlphabeticCharacter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    @SuppressNode
    Rule AlphanumericCharacter() {
        return FirstOf(AlphabeticCharacter(), Digit());
    }

    @SuppressNode
    Rule Digit() {
        return CharRange('0', '9');
    }

    @SuppressNode
    Rule OptionalWhiteSpace() {
        return ZeroOrMore(AnyOf(" \r\t\f\n"));
    }

    @SuppressNode
    Rule WhiteSpace() {
        return OneOrMore(AnyOf(" \r\t\f\n"));
    }

    @Override
    protected Rule fromStringLiteral(String string) {
        return string.endsWith(" ") ?
                Sequence(String(string.substring(0, string.length() - 1)), WhiteSpace()) :
                String(string);
    }

    <T> T pop(Class<T> clazz) {
        return (T) pop();
    }

    /**
     * Will only return values if at least the first element is from type of declared class
     * Will stop popping elements once an element is not of declared type
     */
    <T> List<T> popAll(Class... classes) {
        ValueStack<Object> valueStack = getContext().getValueStack();
        List<T> result = new ArrayList<T>();
        while (!valueStack.isEmpty() && isInstance(classes, valueStack.peek())) {
            result.add((T) valueStack.pop());
        }
        Collections.reverse(result);

        return result;
    }

    private boolean isInstance(Class[] classes, Object peek) {
        for (Class aClass : classes) {
            if (aClass.isInstance(peek)) {
                return true;
            }
        }
        return false;
    }
}
