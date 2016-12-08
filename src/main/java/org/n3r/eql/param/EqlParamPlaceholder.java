package org.n3r.eql.param;

import com.google.common.base.Splitter;
import lombok.Data;
import lombok.val;
import org.n3r.eql.util.S;

import java.sql.Types;

@Data
public class EqlParamPlaceholder {
    public enum InOut {IN, OUT, INOUT;}
    public enum Like {None, Like, LeftLike, RightLike;}

    private int outType = Types.VARCHAR;
    private boolean lob;
    private Like like = Like.None;
    private boolean numberColumn;
    private String placeholder;
    private InOut inOut = InOut.IN;
    private PlaceholderType placeholderType;
    private int seq;

    public void parseOption(String placeHolderOption) {
        val splitter = Splitter.on(',').omitEmptyStrings().trimResults();
        val optionParts = splitter.split(S.upperCase(placeHolderOption));
        for (String optionPart : optionParts) {
            String upperPureOption = parsePureOption(optionPart);
            String upperSubOption = parseSubOption(optionPart);
            if (S.equals("OUT", upperPureOption)) {
                setInOut(InOut.OUT);
                parseOutType(upperSubOption);
            } else if (S.equals("INOUT", upperPureOption)) {
                setInOut(InOut.INOUT);
                parseOutType(upperSubOption);
            } else if (S.equals("LOB", upperPureOption)) setLob(true);
            else if (S.equals("LIKE", upperPureOption)) setLike(Like.Like);
            else if (S.equals("LEFTLIKE", upperPureOption)) setLike(Like.LeftLike);
            else if (S.equals("RIGHTLIKE", upperPureOption)) setLike(Like.RightLike);
            else if (S.equals("NUMBER", upperPureOption)) setNumberColumn(true);
        }
    }

    private void parseOutType(String subOptionUpper) {
        if (subOptionUpper == null) return;

        // ref: http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/mapping.html
        if ("LONG".equals(subOptionUpper)) setOutType(Types.BIGINT);
        else if ("INT".equals(subOptionUpper)) setOutType(Types.INTEGER);
    }

    private String parseSubOption(String optionPart) {
        int leftBrace = optionPart.indexOf('(');
        if (leftBrace == -1) return null;

        int rightBrace = optionPart.indexOf(')', leftBrace);
        if (rightBrace == -1)
            throw new RuntimeException("option " + optionPart + " hasn't right brace");

        return S.trim(optionPart.substring(leftBrace + 1, rightBrace));
    }

    private String parsePureOption(String optionPart) {
        int leftBrace = optionPart.indexOf('(');
        if (leftBrace == -1) return optionPart;

        return S.trim(optionPart.substring(0, leftBrace));
    }
}
