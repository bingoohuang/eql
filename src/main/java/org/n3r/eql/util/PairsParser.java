package org.n3r.eql.util;

import com.google.common.collect.Maps;

import java.util.Map;

public class PairsParser {
    private enum State {None, Key, ValueStart, Value}

    public Map<String, String> parse(String str) {
        Map<String, String> results = Maps.newHashMap();
        char[] chars = str.toCharArray();
        State state = State.None;
        StringBuilder key = new StringBuilder();
        StringBuilder val = new StringBuilder();
        char quoted = '\0';
        boolean escaped = false;

        for (char ch : chars) {
            if (!escaped && ch == '\\') {
                escaped = true;
                continue;
            }

            switch (state) {
                case None:
                    if (Character.isWhitespace(ch)) continue;
                    state = State.Key;
                    key.append(ch);
                    break;
                case Key:
                    if (ch == '=') state = State.ValueStart;
                    else key.append(ch);
                    break;
                case ValueStart:
                    if (Character.isWhitespace(ch)) continue;
                    if ('\'' == ch || '\"' == ch) {
                        quoted = ch;
                        state = State.Value;
                    } else {
                        state = State.Value;
                        val.append(ch);
                    }
                    break;
                case Value:
                    if (!escaped && (quoted == ch || quoted == '\0' && Character.isWhitespace(ch))) {
                        state = State.None;
                        results.put(key.toString().trim(),
                                quoted == '\0' ? val.toString().trim() : val.toString());
                        key.delete(0, key.length());
                        val.delete(0, val.length());
                        quoted = '\0';
                    } else {
                        val.append(ch);
                    }
            }

            escaped = false;
        }

        if (state == State.Value) {
            results.put(key.toString().trim(),
                    quoted == '\0' ? val.toString().trim() : val.toString());
        }

        return results;
    }

}
