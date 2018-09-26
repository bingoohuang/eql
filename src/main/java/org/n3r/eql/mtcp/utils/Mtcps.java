package org.n3r.eql.mtcp.utils;

import com.google.common.collect.Maps;
import org.n3r.eql.spec.Spec;
import org.n3r.eql.spec.SpecParser;
import org.n3r.eql.util.O;

import java.util.HashMap;
import java.util.Map;

public class Mtcps {

    public static <T> T createObjectBySpec(String specString, Class<T> assignableClass) {
        try {
            Spec spec = SpecParser.parseSpecLeniently(specString);
            return O.createObject(assignableClass, spec);
        } catch (Exception e) {
            throw new RuntimeException("failed to create object for spec " + specString, e);
        }
    }

    public static Map<String, String> merge(Map<String, String> params, Map<String, String> merged) {
        HashMap<String, String> props = Maps.newHashMap(params);
        props.putAll(merged);

        return props;
    }

    public static String interpret(String template, Map<String, String> params) {
        StringBuilder parsed = new StringBuilder();

        int fromIndex = 0;
        int size = template.length();

        while (fromIndex < size) {
            int leftBracePos = template.indexOf('{', fromIndex);
            int leftBracketPos = template.indexOf('[', fromIndex);

            // none { or [ found
            if (leftBracePos == -1 && leftBracketPos == -1) {
                parsed.append(template.substring(fromIndex));
                break;
            }

            // first { found
            if (leftBracePos != -1 && (leftBracketPos == -1 || leftBracePos < leftBracketPos)) {
                int rightBracePos = template.indexOf('}', leftBracePos + 1);
                if (rightBracePos == -1) throw new RuntimeException("bad format of template " + template);


                String requiredProp = template.substring(leftBracePos + 1, rightBracePos);
                String paramValue = params.remove(requiredProp);
                if (paramValue == null) throw new RuntimeException(requiredProp + " is required");

                parsed.append(template, fromIndex, leftBracePos);
                fromIndex = rightBracePos + 1;
                parsed.append(paramValue);
                continue;
            }

            // first [ found
            if ((leftBracePos == -1 || leftBracePos > leftBracketPos) && leftBracketPos != -1) {
                int rightBracePos = template.indexOf(']', leftBracePos + 1);
                if (rightBracePos == -1) throw new RuntimeException("bad format of template " + template);

                String optionedProps = template.substring(leftBracketPos + 1, rightBracePos);
                String[] usedProps = optionedProps.split(",");
                StringBuilder usedPropsExpr = new StringBuilder();
                for (String usedProp : usedProps) {
                    String propValue = params.remove(usedProp);
                    if (propValue != null) {
                        if (usedPropsExpr.length() > 0) usedPropsExpr.append('&');
                        usedPropsExpr.append(usedProp).append("=").append(propValue);
                    }
                }

                parsed.append(template, fromIndex, leftBracketPos);
                fromIndex = rightBracePos + 1;
                parsed.append(usedPropsExpr);

                continue;
            }

        }

        return parsed.toString();
    }

}
