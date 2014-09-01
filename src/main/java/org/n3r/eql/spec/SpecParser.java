package org.n3r.eql.spec;


import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.*;

public class SpecParser {
    public static Spec parseSpecLeniently(String spec) {
        Spec[] specs = parseSpecs(spec);

        return specs.length == 0 ? null : specs[0];
    }

    public static Spec parseSpec(String spec) {
        Spec[] specs = parseSpecs(spec);
        if (specs.length > 1) {
            throw new RuntimeException("too many spec defined");
        }

        return specs.length == 0 ? null : specs[0];
    }

    public static Spec[] parseSpecs(String specs) {
        char[] chars = specs.toCharArray();
        SpecState specState = SpecState.SpecClose;
        StringBuilder name = new StringBuilder();
        StringBuilder param = new StringBuilder();
        ParamQuoteState paramQuoteState = ParamQuoteState.None;
        ArrayList<Spec> specsDefs = new ArrayList<Spec>();
        Spec spec = null;
        int i = 0;
        char ch = ' ';
        for (int ii = chars.length; i < ii; ++i) {
            ch = chars[i];
            switch (specState) {
                case SpecClose:
                    if (ch == '@') {
                        specState = SpecState.SpecOpen;
                        paramQuoteState = ParamQuoteState.None;
                    }
                    else if (!isWhitespace(ch)) error(specs, i, ch);

                    break;
                case SpecOpen:
                    if (isJavaIdentifierStart(ch)) {
                        specState = SpecState.SpecName;
                        name.append(ch);
                    } else error(specs, i, ch);

                    break;
                case SpecName:
                    if (isJavaIdentifierPart(ch) || ch == '.' || ch == '$') name.append(ch);
                    else if (isWhitespace(ch) || ch == '@' || ch == '(') {
                        while (i < ii && isWhitespace(chars[i])) ++i;
                        if (i < ii) ch = chars[i];

                        specState = ch == '(' ? SpecState.ParamOpen : SpecState.SpecClose;
                        spec = addSpec(name, specsDefs);

                        if (specState == SpecState.SpecClose) --i; // backspace and continue
                    } else error(specs, i, ch);

                    break;
                case ParamOpen:
                    switch (ch) {
                        case ')':
                            addSpecParam(param, paramQuoteState, spec);
                            specState = SpecState.SpecClose;
                            break;
                        case '"':
                            paramQuoteState = ParamQuoteState.Left;
                            specState = SpecState.ParamValue;
                            clear(param);
                            break;
                        case '\\':
                            ch = convertpecialChar(chars[++i]);
                        default:
                            if (!isWhitespace(ch)) {
                                param.append(ch);
                                specState = SpecState.ParamValue;
                            }
                    }
                    break;
                case ParamValue:
                    switch (ch) {
                        case ')':
                            if (paramQuoteState == ParamQuoteState.Left) param.append(ch);
                            else {
                                addSpecParam(param, paramQuoteState, spec);
                                specState = SpecState.SpecClose;
                            }
                            break;
                        case ',':
                            if (paramQuoteState == ParamQuoteState.Left) param.append(ch);
                            else {
                                addSpecParam(param, paramQuoteState, spec);
                                paramQuoteState = ParamQuoteState.None;
                                specState = SpecState.ParamOpen;
                            }
                            break;
                        case '"':
                            if (paramQuoteState == ParamQuoteState.Left)
                                paramQuoteState = ParamQuoteState.Right;
                            else error(specs, i, ch);
                            break;
                        case '\\':
                            ch = convertpecialChar(chars[++i]);
                        default:
                            if (paramQuoteState == ParamQuoteState.Right) {
                                if (!isWhitespace(ch)) error(specs, i, ch);
                            }
                            else param.append(ch);
                    }
                    break;
                default:
                    error(specs, i, ch);
            }
        }

        // Check whether it is normal ended
        switch (specState) {
            case SpecName:
                addSpec(name, specsDefs);
                break;
            case SpecOpen:
            case ParamValue:
            case ParamOpen:
                error(specs, i, ch);
        }


        return specsDefs.toArray(new Spec[0]);
    }

    private static Spec addSpec(StringBuilder name, List<Spec> specsDefs) {
        Spec spec = new Spec();
        spec.setName(name.toString());
        clear(name);
        specsDefs.add(spec);

        return spec;
    }

    private static void addSpecParam(StringBuilder param, ParamQuoteState paramInQuote, Spec spec) {
        spec.addParam(paramInQuote == ParamQuoteState.Right ? param.toString() : trimSubstring(param));
        clear(param);
    }

    private static StringBuilder clear(StringBuilder param) {
        return param.delete(0, param.length());
    }

    public static String trimSubstring(StringBuilder sb) {
        int first = 0, last = sb.length();

        for (int ii = sb.length(); first < ii; first++)
            if (!isWhitespace(sb.charAt(first))) break;

        for (; last > first; last--)
            if (!isWhitespace(sb.charAt(last - 1))) break;

        return sb.substring(first, last);
    }

    private static Spec[] error(String specs, int i, char ch) {
        throw new RuntimeException(specs + " is invalid at pos " + i + " with char " + ch);
    }

    private static char convertpecialChar(char aChar) {
        switch (aChar) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
        }

        return aChar;
    }

    private static enum SpecState {SpecOpen, SpecName, ParamOpen, ParamValue, SpecClose}

    private static enum ParamQuoteState {None, Left, Right}
}
