package generation.parser.css;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2014, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

import org.parboiled.*;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.support.ValueStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Adapted version of https://github.com/geotools/geotools/blob/master/modules/unsupported/css/src/main/java/org/geotools/styling/css/CssParser.java
 * but without statements that make use of the generation.parser value stack and with a public 'CssRule' method declaration.
 * This is only used for checking if the input matches a CssRule.
 */
@BuildParseTree
public class CssParser extends BaseParser<Object> {

    /**
     * Matches a environment variable expression
     */
    private static final Pattern ENV_PATTERN = Pattern.compile("@([\\w\\d]+)(\\(([^\\)]+)\\))?");

    static CssParser INSTANCE;

    static final Object MARKER = new Object();

    static final class Prefix {
        String prefix;

        public Prefix(java.lang.String prefix) {
            super();
            this.prefix = prefix;
        }
    }

    /**
     * Allows Parboiled to do its magic, while disallowing normal users from instantiating this
     * class
     */
    protected CssParser() {
    }

    /**
     * Returns the single instance of the CSS generation.parser. The CSSParser should not be instantiated
     * directly, Parboiled needs to do it instead.
     *
     * @return
     */
    public static CssParser getInstance() {
        // we need to lazily create it, otherwise Parboiled won't be able to instrument the class
        if (INSTANCE == null) {
            INSTANCE = Parboiled.createParser(CssParser.class);
        }

        return INSTANCE;
    }

    public Rule StyleSheet() {
        return Sequence(
                ZeroOrMore(Directive(), OptionalWhiteSpace()),
                OneOrMore(CssRule()),
                WhiteSpaceOrIgnoredComment()
        );
    }

    Rule Directive() {
        return Sequence(
                "@",
                Identifier(),
                WhiteSpace(),
                String(),
                Ch(';'));
    }

    Rule CssRule() {
        return Sequence(
                WhiteSpaceOrComment(),
                Selector(),
                OptionalWhiteSpace(), //
                '{',
                OptionalWhiteSpace(), //
                RuleContents(),
                WhiteSpaceOrIgnoredComment(),
                '}'
        );
    }

    Rule RuleContents() {
        return Sequence(
                FirstOf(CssRule(), Property()),
                ZeroOrMore(
                        Sequence(
                                WhitespaceOrIgnoredComment(),
                                ';',
                                OptionalWhiteSpace(),
                                FirstOf(CssRule(), Property()))),
                Optional(';')
        );
    }

    Rule Selector() {
        return FirstOf(OrSelector(), AndSelector(), BasicSelector());
    }

    Rule BasicSelector() {
        return FirstOf(
                CatchAllSelector(),
                MinScaleSelector(),
                MaxScaleSelector(),
                IdSelector(),
                PseudoClassSelector(),
                NumberedPseudoClassSelector(),
                TypenameSelector(),
                ECQLSelector());
    }

    Rule AndSelector() {
        return Sequence(
                BasicSelector(),
                OptionalWhiteSpace(),
                FirstOf(AndSelector(), BasicSelector())
        );
    }

    Rule OrSelector() {
        return Sequence(
                FirstOf(AndSelector(), BasicSelector()),
                OptionalWhiteSpace(),
                ',',
                OptionalWhiteSpace(),
                Selector()
        );
    }

    @SuppressSubnodes
    Rule PseudoClassSelector() {
        return Sequence(':', ClassName());
    }

    @SuppressSubnodes
    Rule NumberedPseudoClassSelector() {
        return Sequence(
                ":nth-",
                ClassName(),
                '(',
                Number()
        );
    }

    Rule ClassName() {
        return FirstOf("mark", "stroke", "fill", "symbol", "shield");
    }

    @SuppressSubnodes
    Rule TypenameSelector() {
        return QualifiedIdentifier();
    }

    Rule QualifiedIdentifier() {
        return Sequence(Identifier(), Optional(':', Identifier()));
    }

    @SuppressSubnodes
    Rule IdSelector() {
        return Sequence(
                '#',
                Sequence(
                        Identifier(),
                        Optional(':', Identifier()),
                        Optional('.', Sequence(TestNot(AnyOf("\"'[]")), ANY)))
        );
    }

    Rule CatchAllSelector() {
        return String('*');
    }

    Rule MaxScaleSelector() {
        return Sequence(
                "[",
                OptionalWhiteSpace(),
                FirstOf("@scale", "@sd"),
                OptionalWhiteSpace(),
                FirstOf("<=", "<"),
                OptionalWhiteSpace(),
                ScaleNumber(),
                OptionalWhiteSpace(),
                "]");
    }

    Rule MinScaleSelector() {
        return Sequence(
                "[",
                OptionalWhiteSpace(),
                FirstOf("@scale", "@sd"),
                OptionalWhiteSpace(),
                FirstOf(">=", ">"),
                OptionalWhiteSpace(),
                ScaleNumber(),
                OptionalWhiteSpace(),
                "]");
    }

    Rule WhitespaceOrIgnoredComment() {
        return ZeroOrMore(FirstOf(WhiteSpace(), IgnoredComment()));
    }

    Rule Property() {
        return Sequence(
                WhiteSpaceOrIgnoredComment(),
                Identifier(),
                OptionalWhiteSpace(),
                Colon(),
                OptionalWhiteSpace(), //
                Sequence(
                        Value(),
                        OptionalWhiteSpace(),
                        ZeroOrMore(',', OptionalWhiteSpace(), Value()))
        );
    }

    Rule KeyValue() {
        return Sequence(
                Identifier(),
                OptionalWhiteSpace(),
                Colon(),
                OptionalWhiteSpace(),
                Value()
        );
    }

    @SuppressNode
    Rule Colon() {
        return Ch(':');
    }

    Rule Value() {
        return FirstOf(MultiValue(), SimpleValue());
    }

    Rule SimpleValue() {
        return FirstOf(
                None(),
                URLFunction(),
                TransformFunction(),
                Function(),
                Color(),
                Measure(),
                ValueIdentifier(),
                VariableValue(),
                MixedExpression());
    }

    Rule None() {
        return String("none");
    }

    Rule VariableValue() {
        return Sequence(
                Ch('@'),
                Identifier(),
                Optional(
                        Sequence(
                                Ch('('),
                                OneOrMore(Sequence(TestNot(AnyOf(")")), ANY)),
                                Ch(')')))
        );
    }

    Rule MixedExpression() {
        return OneOrMore(
                FirstOf(ECQLExpression(), String()),
                Optional(
                        FirstOf(
                                String("px"),
                                String("m"),
                                String("ft"),
                                String("%"),
                                String("deg")))
        );
    }

    Rule MultiValue() {
        return Sequence(
                SimpleValue(),
                OneOrMore(WhiteSpace(), SimpleValue())
        );
    }

    Rule Function() {
        return Sequence(
                Identifier(),
                '(',
                Value(),
                ZeroOrMore(OptionalWhiteSpace(), ',', OptionalWhiteSpace(), Value()),
                ')'
        );
    }

    Rule TransformFunction() {
        return Sequence(
                QualifiedIdentifier(),
                '(',
                Optional(OptionalWhiteSpace(), KeyValue()),
                ZeroOrMore(OptionalWhiteSpace(), ',', OptionalWhiteSpace(), KeyValue()),
                ')');
    }

    Rule URLFunction() {
        return Sequence(
                "url",
                OptionalWhiteSpace(),
                "(",
                OptionalWhiteSpace(),
                URL(),
                OptionalWhiteSpace(),
                ")");
    }

    /**
     * Very relaxed URL matcher, as we need to match also relative urls
     *
     * @return
     */
    Rule URL() {
        return FirstOf(QuotedURL(), SimpleURL());
    }

    Rule SimpleURL() {
        return OneOrMore(FirstOf(Alphanumeric(), AnyOf("-._]:/?#[]@|$&'*+,;=")));
    }

    Rule QuotedURL() {
        // same as simple url, but with ' surrounding it, and not within the url itlsef
        return Sequence(
                "'",
                Sequence(
                        OneOrMore(FirstOf(Alphanumeric(), AnyOf("-._]:/?#[]@|$&*+,;="))),
                        "'")
        );
    }

    Rule ValueIdentifier() {
        return Identifier();
    }

    Rule String() {
        return FirstOf(
                Sequence(
                        '\'',
                        ZeroOrMore(Sequence(TestNot(AnyOf("'\\")), ANY)),
                        '\''),
                Sequence(
                        '"',
                        ZeroOrMore(Sequence(TestNot(AnyOf("\"\\")), ANY)),
                        '"'));
    }

    Rule Measure() {
        return
                Sequence(
                        Number(),
                        Optional(
                                FirstOf(
                                        String("k"),
                                        String("M"),
                                        String("G"),
                                        String("px"),
                                        String("m"),
                                        String("ft"),
                                        String("%"),
                                        String("deg")))
                );
    }


    Rule ECQLExpression() {
        return ECQL(
                new Action() {
                    @Override
                    public boolean run(Context ctx) {
                        return true;
                    }
                });
    }

    Rule ECQLSelector() {
        return ECQL(
                new Action() {
                    @Override
                    public boolean run(Context ctx) {
                        return true;
                    }
                });
    }

    Rule ECQL(Action parserChecker) {
        return Sequence(
                '[',
                OneOrMore(
                        FirstOf(
                                SingleQuotedString(),
                                DoubleQuotedString(),
                                Sequence(TestNot(AnyOf("\"'[]")), ANY))), //
                parserChecker,
                ']');
    }

    Rule DoubleQuotedString() {
        return Sequence('"', ZeroOrMore(Sequence(TestNot(AnyOf("\r\n\"\\")), ANY)), '"');
    }

    Rule SingleQuotedString() {
        return Sequence('\'', ZeroOrMore(Sequence(TestNot(AnyOf("\r\n'\\")), ANY)), '\'');
    }

    Rule IntegralNumber() {
        return OneOrMore(Digit());
    }

    Rule Number() {
        return Sequence(
                Optional(AnyOf("-+")), OneOrMore(Digit()), Optional('.', ZeroOrMore(Digit())));
    }

    Rule ScaleNumber() {
        return Sequence(
                OneOrMore(Digit()), Optional('.', ZeroOrMore(Digit())), Optional(AnyOf("kMG")));
    }

    Rule ScaleValue() {
        return Sequence(
                OneOrMore(Digit()),
                Optional('.', ZeroOrMore(Digit())),
                AnyOf("kMG")
        );
    }

    @SuppressSubnodes
    Rule Color() {
        return
                Sequence(
                        '#',
                        FirstOf(
                                Sequence(Hex(), Hex(), Hex(), Hex(), Hex(), Hex()),
                                Sequence(Hex(), Hex(), Hex())));
    }

    String toHexColor(String hex) {
        if (hex.length() == 7) {
            return hex;
        } else {
            char r = hex.charAt(1);
            char g = hex.charAt(2);
            char b = hex.charAt(3);
            return "#" + r + r + g + g + b + b;
        }
    }

    @SuppressNode
    Rule Identifier() {
        return Sequence(Optional('-'), NameStart(), ZeroOrMore(NameCharacter()));
    }

    @SuppressNode
    Rule NameStart() {
        return FirstOf('_', Alpha());
    }

    @SuppressNode
    Rule NameCharacter() {
        return FirstOf(AnyOf("-_"), Alphanumeric());
    }

    @SuppressNode
    Rule Hex() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), Digit());
    }

    @SuppressNode
    Rule Digit() {
        return CharRange('0', '9');
    }

    @SuppressNode
    Rule Alphanumeric() {
        return FirstOf(Alpha(), Digit());
    }

    @SuppressNode
    Rule Alpha() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'));
    }

    Rule IgnoredComment() {
        return Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/");
    }

    Rule RuleComment() {
        return Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/");
    }

    @SuppressNode
    Rule WhiteSpaceOrIgnoredComment() {
        return ZeroOrMore(FirstOf(IgnoredComment(), WhiteSpace()));
    }

    @SuppressNode
    Rule WhiteSpaceOrComment() {
        return ZeroOrMore(FirstOf(RuleComment(), WhiteSpace()));
    }

    @SuppressNode
    Rule OptionalWhiteSpace() {
        return ZeroOrMore(AnyOf(" \r\t\f\n"));
    }

    @SuppressNode
    Rule WhiteSpace() {
        return OneOrMore(AnyOf(" \r\t\f\n"));
    }

    /**
     * We redefine the rule creation for string literals to automatically match trailing whitespace
     * if the string literal ends with a space character, this way we don't have to insert extra
     * whitespace() rules after each character or string literal
     */
    @Override
    protected Rule fromStringLiteral(String string) {
        return string.matches("\\s+$")
                ? Sequence(String(string.substring(0, string.length() - 1)), OptionalWhiteSpace())
                : String(string);
    }

    <T> T pop(Class<T> clazz) {
        return (T) pop();
    }

    <T> List<T> popAll(Class... classes) {
        ValueStack<Object> valueStack = getContext().getValueStack();
        List<T> result = new ArrayList<T>();
        while (!valueStack.isEmpty() && isInstance(classes, valueStack.peek())) {
            result.add((T) valueStack.pop());
        }
        if (!valueStack.isEmpty() && valueStack.peek() == MARKER) {
            valueStack.pop();
        }
        Collections.reverse(result);

        return result;
    }

    private boolean isInstance(Class[] classes, Object peek) {
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].isInstance(peek)) {
                return true;
            }
        }
        return false;
    }
}
