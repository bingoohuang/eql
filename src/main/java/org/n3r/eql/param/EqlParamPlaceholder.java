package org.n3r.eql.param;

import com.google.common.base.Splitter;
import lombok.Data;
import lombok.val;
import org.n3r.eql.param.EqlParamsParser.PlaceHolderTemp;
import org.n3r.eql.util.S;

import java.sql.Types;
import java.util.regex.Pattern;

import static org.n3r.eql.param.EqlParamsParser.SUB;

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
    private boolean escape;
    private String escapeValue;

    public void parseOption(PlaceHolderTemp holder, String evalSqlTemplate) {
        val splitter = Splitter.on(',').omitEmptyStrings().trimResults();
        val optionParts = splitter.split(S.upperCase(holder.getPlaceHolderOptions()));
        for (String optionPart : optionParts) {
            String upperPureOption = parsePureOption(optionPart);
            String upperSubOption = parseSubOption(optionPart);
            if (S.equals("OUT", upperPureOption)) {
                setInOut(InOut.OUT);
                parseOutType(upperSubOption);
            } else if (S.equals("INOUT", upperPureOption)) {
                setInOut(InOut.INOUT);
                parseOutType(upperSubOption);
            } else if (S.equals("LOB", upperPureOption)) {
                setLob(true);
            } else if (S.equals("LIKE", upperPureOption)) {
                setLike(Like.Like);
                parseEscape(holder, evalSqlTemplate);
            } else if (S.equals("LEFTLIKE", upperPureOption)) {
                setLike(Like.LeftLike);
                parseEscape(holder, evalSqlTemplate);
            } else if (S.equals("RIGHTLIKE", upperPureOption)) {
                setLike(Like.RightLike);
                parseEscape(holder, evalSqlTemplate);
            } else if (S.equals("NUMBER", upperPureOption)) {
                setNumberColumn(true);
            }
        }
    }

    private void parseEscape(PlaceHolderTemp holder, String evalSqlTemplate) {
        val fromSub = S.wrap(holder.getQuestionSeq(), SUB);
        int fromSubIndex = evalSqlTemplate.indexOf(fromSub) + fromSub.length();

        val escapePattern = Pattern.compile("\\s+ESCAPE\\s+(\\S+)",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        val substring = evalSqlTemplate.substring(fromSubIndex);
        val matcher = escapePattern.matcher(substring);
        setEscape(matcher.find());

        if (!isEscape()) return;

        val val = S.unQuote(matcher.group(1), "'");
        setEscapeValue(S.isNotEmpty(val) ? val.substring(0, 1) : "\\");
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
